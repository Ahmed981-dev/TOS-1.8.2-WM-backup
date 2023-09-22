package com.android.services.models.im

import com.android.services.db.entities.ZaloMessageRooted
import com.android.services.db.entities.ZaloPostRooted
import com.google.gson.annotations.SerializedName

class ZaloLogs {

    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("zaloPosts")
    var zaloPostList: List<ZaloPostRooted>? = null
        private set

    @SerializedName("zaloMessages")
    var zaloMessageList: List<ZaloMessageRooted>? = null
        private set

    constructor()
    constructor(
        userId: String?,
        phoneServiceId: String?,
        zaloPostList: List<ZaloPostRooted>?,
        zaloUnrootedList: List<ZaloMessageRooted>?
    ) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        this.zaloPostList = zaloPostList
        zaloMessageList = zaloUnrootedList
    }
}