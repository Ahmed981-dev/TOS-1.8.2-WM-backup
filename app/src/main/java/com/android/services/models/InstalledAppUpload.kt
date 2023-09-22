package com.android.services.models

import com.android.services.db.entities.InstalledApp

data class InstalledAppUpload(
    val userId: String,
    val phoneServiceId: String,
    val installedApps: List<InstalledApp>
)
