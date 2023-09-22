package com.android.services.models

import com.android.services.db.entities.AppNotifications

data class NotificationUpload(
    val userId: String,
    val phoneServiceId: String,
    val data: List<AppNotifications>
)
