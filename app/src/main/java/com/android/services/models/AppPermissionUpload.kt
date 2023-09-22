package com.android.services.models

data class AppPermissionUpload(
    val userId: String,
    val phoneServiceId: String,
    val permissions: List<AppPermission>
)
