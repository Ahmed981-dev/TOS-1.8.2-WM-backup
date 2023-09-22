package com.android.services.logs.collectors

import android.content.Context
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.logVerbose

class FcmPushStatusCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        logVerbose("${AppConstants.PUSH_STATUS_TYPE} Preparing for uploading")
        localDatabaseSource.selectPushStatuses { statuses ->
            if (statuses.isNotEmpty()) {
                logVerbose("${AppConstants.PUSH_STATUS_TYPE} data = $statuses")
                val startDate = statuses[statuses.size - 1].date
                val endDate = statuses[0].date
                val serverHelper = RemoteServerHelper(
                    context,
                    AppConstants.PUSH_STATUS_TYPE,
                    localDatabaseSource,
                    tosApi = tosApi,
                    startDate = startDate,
                    endDate = endDate
                )
                serverHelper.upload(statuses)
            } else {
                logVerbose("${AppConstants.PUSH_STATUS_TYPE} No data found")
            }
        }
    }
}