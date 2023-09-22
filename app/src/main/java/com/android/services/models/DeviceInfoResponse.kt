package com.android.services.models

import com.android.services.db.entities.PhoneServices

data class DeviceInfoResponse(
    val statusCode:String,
    val screenRecording:String,
    val voipRecording:String,
    val syncSettings: SyncSetting,
    val expiryDate:String?,
    val userInfo:List<PhoneServices>?,
)