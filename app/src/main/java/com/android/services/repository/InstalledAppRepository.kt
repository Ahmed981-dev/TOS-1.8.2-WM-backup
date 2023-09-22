package com.android.services.repository

import com.android.services.db.dao.InstalledAppsDao
import com.android.services.db.entities.InstalledApp
import com.android.services.models.UninstalledApp
import javax.inject.Inject

class InstalledAppRepository @Inject constructor(private val installedAppsDao: InstalledAppsDao) {

    fun insertInstalledApp(installedApp: List<InstalledApp>) {
        installedAppsDao.insert(installedApp)
    }

    fun selectInstalledApps(): List<InstalledApp> {
        return installedAppsDao.selectAllInstalledApps(0)
    }

    fun updateInstalledApp(startId: Int, endId: Int) {
        installedAppsDao.update(startId, endId, 1)
    }

    fun delete(packageName: String) {
        installedAppsDao.delete(packageName)
    }

    fun getUninstallAppsList(): List<UninstalledApp> {
        return installedAppsDao.selectUninstalledApps(1)
    }

    fun setAppAsUninstalled(packageName: String) {
        installedAppsDao.update(packageName, 1)
    }

    fun checkIfNotExistsAlready(packageName: String): Boolean {
        return installedAppsDao.checkIfAlreadyExist(packageName) == null
    }
}