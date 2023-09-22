package com.android.services.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScreenShotCommand(
    @SerializedName("customData")
    var customData: String = "",
    @SerializedName("intervalOption")
    var intervalOption: String="15",
    @SerializedName("method")
    var method: String = "",
    @SerializedName("push_id")
    var pushId: String = ""
) : Parcelable