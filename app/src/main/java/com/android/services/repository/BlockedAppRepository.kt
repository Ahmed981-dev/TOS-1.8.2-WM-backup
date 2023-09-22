package com.android.services.repository

import android.content.Context
import android.os.AsyncTask
import com.android.services.db.dao.BlockedAppDao
import com.android.services.db.entities.BlockedApp
import javax.inject.Inject

class BlockedAppRepository @Inject constructor(private val blockedAppDao: BlockedAppDao) {

    fun insertBlockedApp(blockedApp: BlockedApp) {
        val url = blockedAppDao.checkAlreadyExists(blockedApp.packageName)
        if (url == null) {
            blockedAppDao.insert(blockedApp)
        } else {
            blockedAppDao.update(blockedApp)
        }
    }

    fun selectBlockedApps(): List<String> = blockedAppDao.selectAllBlockedApps("0")
}