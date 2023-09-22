package com.android.services.repository

import com.android.services.db.dao.PushStatusDao
import com.android.services.db.entities.PushStatus
import javax.inject.Inject

class PushStatusRepository @Inject constructor(private val pushStatusDao: PushStatusDao) {

    fun checkIfPushNotExistsAlready(pushId: String): Boolean =
        pushStatusDao.checkIfAlreadyExist(pushId) == null

    fun insertPushStatus(pushStatus: PushStatus) = pushStatusDao.insert(pushStatus)
    fun selectPushStatuses(callback: (List<PushStatus>) -> Unit) =
        callback(pushStatusDao.selectAllPushStatuses(0))

    fun updatePushStatus(pushStatus: PushStatus) = pushStatusDao.update(pushStatus)

}