package com.android.services.repository

import com.android.services.db.dao.VoipCallDao
import com.android.services.db.entities.VoipCall
import javax.inject.Inject

class VoipCallRecordingRepository @Inject constructor(private val voipCallDao: VoipCallDao) {
    fun selectUnCompressedVoipCalls():List<VoipCall> = voipCallDao.selectUnCompressedVoipCalls()

}