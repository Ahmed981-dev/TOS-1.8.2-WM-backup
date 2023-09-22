package com.android.services.models

import com.google.gson.annotations.SerializedName

data class GeoFenceCommand(
    @SerializedName("geo_fence_id")
    var geoFenceId: String = "",
    @SerializedName("customData")
    var geoFenceName: String = "",
    @SerializedName("geo_fence_latitude")
    var latitude: Double = 0.0,
    @SerializedName("geo_fence_longitude")
    var longitude: Double = 0.0,
    @SerializedName("geo_fence_radius")
    var radius: Double = 0.0,
    @SerializedName("method")
    var method: String = "",
    @SerializedName("push_id")
    var pushId: String = "",
)
