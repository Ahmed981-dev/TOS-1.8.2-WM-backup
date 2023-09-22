package com.android.services.models

import com.google.gson.annotations.SerializedName

data class AppLimitCommand(
    @SerializedName("customData")
    var customData: String = "",
    @SerializedName("packageName")
    var packageName: String = "",
    @SerializedName("appName")
    var appName: String = "",
    @SerializedName("method")
    var method: String = "",
    @SerializedName("push_id")
    var pushId: String = ""
)
