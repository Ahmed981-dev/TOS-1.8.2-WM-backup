package com.android.services.repository

import com.android.services.db.TextAlertEventDao
import com.android.services.db.dao.MicBugDao
import com.android.services.db.entities.MicBug
import javax.inject.Inject

class MicBugRepository @Inject constructor(private val micBugDao: MicBugDao) {
    fun selectUnCompressedMicBugs(): List<MicBug> = micBugDao.selectUnCompressedMicBugs()

}