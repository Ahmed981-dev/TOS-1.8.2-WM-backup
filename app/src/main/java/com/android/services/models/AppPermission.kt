package com.android.services.models

import com.google.gson.annotations.SerializedName

class AppPermission {

    @SerializedName("permission")
    var permission: String? = null

    @SerializedName("status")
    var status: Boolean? = null

    constructor()
    constructor(permission: String?, status: Boolean?) {
        this.permission = permission
        this.status = status
    }
}