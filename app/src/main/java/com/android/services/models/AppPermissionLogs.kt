package com.android.services.models

import com.google.gson.annotations.SerializedName

class AppPermissionLogs {

    @SerializedName("userId")
    var userId: String? = null

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null

    @SerializedName("permissions")
    var permissions: List<AppPermission>? = null

    constructor()
    constructor(userId: String?, phoneServiceId: String?, appPermissionLogs: List<AppPermission>?) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        permissions = appPermissionLogs
    }
}