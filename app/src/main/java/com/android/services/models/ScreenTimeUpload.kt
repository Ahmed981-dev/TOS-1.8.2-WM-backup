package com.android.services.models

import com.google.gson.annotations.SerializedName

data class ScreenTimeUpload(
    @SerializedName("userId")
    private var userId: String,
    @SerializedName("phoneServiceId")
    private val phoneServiceId: String,
    @SerializedName("data")
    private val screenTimes: List<ScreenTimeModel>
)