package com.android.services.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.services.repository.InstalledAppRepository
import com.android.services.util.InjectorUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UninstallAppReceiver : BroadcastReceiver() {
    private lateinit var installedAppRepository: InstalledAppRepository
    private var intentData = ""
    override fun onReceive(context: Context, intent: Intent?) {
        intentData = intent?.data?.toString() ?: ""
        val uninstalledAppPackage = if (intentData.isNotEmpty()) {
            if (intentData.contains(":")) {
                intentData.split(":")[1]
            } else {
                intentData
            }
        } else ""
        CoroutineScope(Dispatchers.IO).launch {
            installedAppRepository = InjectorUtils.provideInstalledAppsRepository(context)
            val isPackageExistInUninstalledApps =
                !installedAppRepository.checkIfNotExistsAlready(uninstalledAppPackage)
            if (isPackageExistInUninstalledApps) {
                installedAppRepository.delete(uninstalledAppPackage)
            }
        }
    }
}