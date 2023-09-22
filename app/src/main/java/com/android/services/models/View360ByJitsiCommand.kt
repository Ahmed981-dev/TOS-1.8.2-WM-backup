package com.android.services.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class View360ByJitsiCommand(
    @SerializedName("customData")
    var customData: String = "",
    @SerializedName("method")
    var method: String = "",
    @SerializedName("push_id")
    var pushId: String = "",
    @SerializedName("cameraType")
    var cameraType: String = "",
    @SerializedName("url")
    var url: String = "",
    @SerializedName("audioType")
    var audioType: String = "",
    @SerializedName("homeToken")
    var homeToken:String="",
    @SerializedName("roomName")
    var roomName:String=""

) : Parcelable