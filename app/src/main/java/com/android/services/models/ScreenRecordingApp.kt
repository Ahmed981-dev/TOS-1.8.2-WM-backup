package com.android.services.models

import com.google.gson.annotations.SerializedName

class ScreenRecordingApp {
    @SerializedName("appPackage")
    var appPackage: String? = null
    @SerializedName("appName")
    var appName: String? = null
    @SerializedName("isEnabled")
    var isEnabled = false
}