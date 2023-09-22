package com.android.services.models.im

import com.android.services.db.entities.HangoutRooted
import com.google.gson.annotations.SerializedName

class HangoutLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("hangoutsLogs")
    var hangoutList: List<HangoutRooted>? = null
        private set

    constructor()
    constructor(
        userId: String?,
        phoneServiceId: String?,
        hangoutUnrootedList: List<HangoutRooted>?
    ) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        hangoutList = hangoutUnrootedList
    }
}