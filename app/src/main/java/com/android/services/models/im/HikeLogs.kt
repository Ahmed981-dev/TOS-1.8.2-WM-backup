package com.android.services.models.im

import com.android.services.db.entities.HikeRooted
import com.google.gson.annotations.SerializedName

class HikeLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("hikeLogs")
    var hikeList: List<HikeRooted>? = null
        private set

    constructor()
    constructor(userId: String?, phoneServiceId: String?, hikeUnrootedList: List<HikeRooted>?) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        hikeList = hikeUnrootedList
    }
}