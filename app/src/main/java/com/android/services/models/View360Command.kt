package com.android.services.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class View360Command(
    @SerializedName("customData")
    var customData: String = "",
    @SerializedName("method")
    var method: String = "",
    @SerializedName("push_id")
    var pushId: String = "",
    @SerializedName("cameraType")
    var cameraType: String = "",
    @SerializedName("url")
    var url: String = ""
) : Parcelable