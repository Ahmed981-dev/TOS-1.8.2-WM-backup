package com.android.services.models

import com.android.services.db.entities.CallLog

data class CallLogUpload(
    val userId: String,
    val phoneServiceId: String,
    val callLogs: List<CallLog>
)