package com.android.services.repository

import com.android.services.db.dao.RestrictedCallDao
import com.android.services.db.entities.RestrictedCall
import javax.inject.Inject

class RestrictedCallRepository @Inject constructor(private val restrictedCallDao: RestrictedCallDao) {

    fun insertRestrictedCall(restrictedCall: RestrictedCall) {
        val url = restrictedCallDao.checkAlreadyExists(restrictedCall.number)
        if (url == null) {
            restrictedCallDao.insert(restrictedCall)
        } else {
            restrictedCallDao.update(restrictedCall)
        }
    }

    fun deleteRestrictedCall(number: String) {
        restrictedCallDao.delete(number)
    }

    fun selectRestrictedCalls(): List<String> {
        return restrictedCallDao.selectAllRestrictedCalls("1")
    }

}