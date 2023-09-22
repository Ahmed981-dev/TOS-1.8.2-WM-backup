package com.android.services.repository

import com.android.services.db.dao.SmsLogDao
import com.android.services.db.entities.SmsLog
import javax.inject.Inject

class SmsLogRepository @Inject constructor(private val smsLogDao: SmsLogDao) {

    fun checkSmsNotAlreadyExists(smsId : String) = smsLogDao.checkIfAlreadyExist(smsId) == null
    fun insert(smsLog: SmsLog) = smsLogDao.insert(smsLog)
    fun getAllSmsWithMessageBody(messageBody: String, callback: (List<SmsLog>) -> Unit) =
        callback(smsLogDao.getAllSmsWithMessageBody(messageBody))


}