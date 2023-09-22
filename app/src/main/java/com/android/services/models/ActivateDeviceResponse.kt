package com.android.services.models

data class ActivateDeviceResponse(
    val statusCode: String,
    val userId: String,
    val phoneServiceId: String
)