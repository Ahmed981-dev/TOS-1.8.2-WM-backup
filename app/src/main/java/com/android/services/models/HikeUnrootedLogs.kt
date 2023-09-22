package com.android.services.models

import com.android.services.db.entities.HikeUnrooted
import com.google.gson.annotations.SerializedName

class HikeUnrootedLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("data")
    var hikeUnrootedList: List<HikeUnrooted>? = null
        private set

    constructor()
    constructor(userId: String?, phoneServiceId: String?, hikeUnrootedList: List<HikeUnrooted>?) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        this.hikeUnrootedList = hikeUnrootedList
    }
}