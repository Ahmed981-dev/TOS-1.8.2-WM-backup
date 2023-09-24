package com.android.services.repository

import com.android.services.db.dao.CallRecordingDao
import com.android.services.db.entities.CallRecording
import javax.inject.Inject

class CallRecordRepository @Inject constructor(private val callRecordingDao:CallRecordingDao) {
    fun selectUnCompressedCallRecording(): List<CallRecording> =
        callRecordingDao.selectUnCompressedFiles()
}