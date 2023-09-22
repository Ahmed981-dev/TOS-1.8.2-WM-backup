package com.android.services.repository

import com.android.services.db.dao.ScreenRecordingDao
import com.android.services.db.entities.ScreenRecording
import javax.inject.Inject

class ScreenRecordingRepository @Inject constructor(private val screenRecordingDao: ScreenRecordingDao) {

    fun insertScreenRecording(screenRecording: ScreenRecording) =
        screenRecordingDao.insert(screenRecording)

    fun selectScreenRecordings(callback: (List<ScreenRecording>) -> Unit) =
        callback(screenRecordingDao.selectAllScreenRecordings(0, true))

    fun updateScreenRecording(file: String) = screenRecordingDao.update(file, 1)
    fun updateScreenRecordingCompressedStatus(file: String) = screenRecordingDao.update(file, true)
    fun selectUncompressedScreenRecordings(): List<ScreenRecording> =
        screenRecordingDao.selectAllScreenRecordings(0, false)
}