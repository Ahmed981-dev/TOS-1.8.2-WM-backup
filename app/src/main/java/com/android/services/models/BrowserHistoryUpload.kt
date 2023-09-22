package com.android.services.models

import com.android.services.db.entities.BrowserHistory

data class BrowserHistoryUpload(
    val userId: String,
    val phoneServiceId: String,
    val browserhistoryLogs: List<BrowserHistory>
)