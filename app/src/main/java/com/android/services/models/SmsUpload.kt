package com.android.services.models

import com.android.services.db.entities.SmsLog

data class SmsUpload(
    val userId: String,
    val phoneServiceId: String,
    val smsLogs: List<SmsLog>
)
