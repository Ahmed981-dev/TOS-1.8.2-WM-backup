package com.android.services.repository

import com.android.services.db.dao.AppLimitDao
import com.android.services.db.entities.AppLimit
import javax.inject.Inject

class AppLimitRepository @Inject constructor(private val appLimitDao: AppLimitDao) {

    fun delete(packageName: String) {
        appLimitDao.delete(packageName)
    }

    fun insertAppLimit(appLimit: AppLimit) {
        appLimitDao.insert(appLimit)
    }

    fun selectAppLimits(): List<AppLimit> {
        return appLimitDao.selectAllAppLimit()
    }

    fun updateAppLimit(appLimit: AppLimit) {
        appLimitDao.update(appLimit)
    }

    fun checkAlreadyExists(packageName: String): AppLimit? {
        return appLimitDao.checkIfAlreadyExists(packageName)
    }
}