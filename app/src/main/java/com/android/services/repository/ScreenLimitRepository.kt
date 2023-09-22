package com.android.services.repository

import android.content.Context
import android.os.AsyncTask
import com.android.services.db.dao.ScreenLimitDao
import com.android.services.db.entities.ScreenLimit
import com.orhanobut.logger.Logger
import javax.inject.Inject

class ScreenLimitRepository @Inject constructor(private val screenLimitDao: ScreenLimitDao) {

    fun delete(screenDay: String) {
        screenLimitDao.delete(screenDay)
    }

    fun insertScreenLimit(screenLimit: ScreenLimit) {
        screenLimitDao.insert(screenLimit)
    }

    fun selectScreenLimits(): List<ScreenLimit> {
        return screenLimitDao.selectAllScreenLimit()
    }

    fun deleteScreenUsageLimit(screenDay: String) {
        screenLimitDao.delete(screenDay)
    }

    fun deleteScreenRangeLimit(day: String) {
        screenLimitDao.removeScreenRangeLimit(day, "", "")
    }

    fun updateScreenLimit(screenLimit: ScreenLimit) {
        screenLimitDao.update(screenLimit)
    }

    fun checkAlreadyExists(day: String): ScreenLimit? {
        return screenLimitDao.checkIfAlreadyExists(day)
    }
}