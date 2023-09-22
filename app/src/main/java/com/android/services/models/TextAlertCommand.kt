package com.android.services.models

import com.google.gson.annotations.SerializedName

data class TextAlertCommand(
    @SerializedName("customData")
    var customData: String = "",
    @SerializedName("category")
    var category: String = "",
    @SerializedName("type")
    var type: String,
    @SerializedName("method")
    var method: String = "",
    @SerializedName("push_id")
    var pushId: String = "",
    @SerializedName("eventThrough")
    var eventThrough: String = "",
     @SerializedName("email")
    var email: String = ""
)
