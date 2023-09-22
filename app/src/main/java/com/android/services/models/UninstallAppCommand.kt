package com.android.services.models

import com.google.gson.annotations.SerializedName

data class UninstallAppCommand(
    @SerializedName("customData")
    var customData: String = "",
    @SerializedName("appName")
    var appName: String = "",
    @SerializedName("packageName")
    var packageName: String = "",
    @SerializedName("method")
    var method: String = "",
    @SerializedName("push_id")
    var pushId: String = ""
)
