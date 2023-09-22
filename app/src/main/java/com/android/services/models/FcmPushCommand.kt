package com.android.services.models

import com.google.gson.annotations.SerializedName

data class FcmPushCommand(
    @SerializedName("customData")
    var customData: String = "",
    @SerializedName("method")
    var method: String = "",
    @SerializedName("push_id")
    var pushId: String = ""
)
