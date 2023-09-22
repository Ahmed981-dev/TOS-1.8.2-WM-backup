package com.android.services.models

import com.android.services.db.entities.TextAlertEvent
import com.google.gson.annotations.SerializedName

data class TextAlertLogs(
    @SerializedName("userId")
    var userId: String = "",
    @SerializedName("phoneServiceId")
    var phoneServiceId: String = "",
    @SerializedName("appAlerts")
    var appAlerts: List<TextAlertEvent> = listOf(),
    @SerializedName("emailAlerts")
    var emailAlerts: List<TextAlertEvent> = listOf()
)