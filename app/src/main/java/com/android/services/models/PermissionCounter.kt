package com.android.services.models

import com.android.services.enums.PermissionScreens

data class PermissionCounter(
    var permission: PermissionScreens,
    var permissionNumber: Int,
    var totalPermissions: Int
)
