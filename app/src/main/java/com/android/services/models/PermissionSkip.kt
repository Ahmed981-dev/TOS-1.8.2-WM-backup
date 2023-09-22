package com.android.services.models

data class PermissionSkip(
    var locationPermission: Boolean = false,
    var managementOfAllFiles: Boolean = false,
    var deviceAdmin: Boolean = false,
    var drawOverApps: Boolean = false,
    var screenRecord: Boolean = false,
    var notificationAccess: Boolean = false,
    var disableNotificationAccess: Boolean = false,
    var usageAccessPermission: Boolean = false,
    var accessibility: Boolean = false
)
