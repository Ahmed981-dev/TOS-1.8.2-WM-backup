package com.android.services.models

import com.google.gson.annotations.SerializedName

data class View360User(
    @SerializedName("psid")
    var phoneServiceId: String,
    @SerializedName("userId")
    var userId: String
)