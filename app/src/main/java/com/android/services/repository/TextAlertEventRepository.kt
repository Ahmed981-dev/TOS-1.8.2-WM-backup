package com.android.services.repository

import com.android.services.db.TextAlertEventDao
import com.android.services.db.entities.TextAlertEvent
import java.util.*
import javax.inject.Inject

class TextAlertEventRepository @Inject constructor(private val textAlertEventDao: TextAlertEventDao) {

    fun insertTextAlert(textAlertEvent: TextAlertEvent) = textAlertEventDao.insert(textAlertEvent)
    fun selectTextAlertEvents(callback: (List<TextAlertEvent>) -> Unit) =
        callback(textAlertEventDao.selectAllTextAlertEvents(0))

    fun updateTextAlerts(startDate: Date, endDate: Date) =
        textAlertEventDao.updateTextAlertEvents(1, startDate, endDate)

}