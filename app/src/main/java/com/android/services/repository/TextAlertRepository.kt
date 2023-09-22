package com.android.services.repository

import com.android.services.db.dao.TextAlertDao
import com.android.services.db.entities.TextAlert

class TextAlertRepository(private val textAlertDao: TextAlertDao) {

    fun insertTextAlert(textAlert: TextAlert) = textAlertDao.insert(textAlert)
    fun selectTextAlerts(callback: (List<TextAlert>) -> Unit) =
        callback(textAlertDao.selectAllTextAlerts())

    fun deleteTextAlert(alertId: String) = textAlertDao.delete(alertId)
    fun updateTextAlert(textAlert: TextAlert) = textAlertDao.update(textAlert)

}