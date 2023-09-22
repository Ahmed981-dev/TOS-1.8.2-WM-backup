package com.android.services.models

import com.android.services.db.entities.GpsLocation

data class GeoLocationUpload(
    val userId: String,
    val phoneServiceId: String,
    val geolocationLogs: List<GpsLocation>
)
