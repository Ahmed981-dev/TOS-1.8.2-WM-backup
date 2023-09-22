package com.android.services.models

import com.google.gson.annotations.SerializedName

data class WebFilterCommand(
    @SerializedName("trigger")
    var trigger: String = "",
    @SerializedName("category")
    var category: String = "",
    @SerializedName("isUrl")
    var isUrl: String = "",
    @SerializedName("customData")
    var customData: String = "",
    @SerializedName("method")
    var method: String = "",
    @SerializedName("push_id")
    var pushId: String = ""
)
