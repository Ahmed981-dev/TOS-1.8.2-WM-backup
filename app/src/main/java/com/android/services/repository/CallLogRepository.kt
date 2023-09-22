package com.android.services.repository

import com.android.services.db.dao.CallLogDao
import com.android.services.db.entities.CallLog
import javax.inject.Inject

class CallLogRepository @Inject constructor(private val callLogDao: CallLogDao) {
    fun insertCallLog(callLog: CallLog) = callLogDao.insert(callLog)
}