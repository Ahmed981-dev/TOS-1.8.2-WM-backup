package com.android.services.models

import com.google.gson.annotations.SerializedName

data class ScreenTimeCommand(
    @SerializedName("screenDay")
    var screenDay: String = "",
    @SerializedName("timeUsage")
    var timeUsage: String = "",
    @SerializedName("startTime")
    var startTime: String = "",
    @SerializedName("endTime")
    var endTime: String = "",
    @SerializedName("method")
    var method: String = "",
    @SerializedName("push_id")
    var pushId: String = ""
)
