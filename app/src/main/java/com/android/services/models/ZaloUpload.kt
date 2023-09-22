package com.android.services.models

import com.android.services.db.entities.ZaloMessageRooted
import com.android.services.db.entities.ZaloPostRooted
import com.google.gson.annotations.SerializedName

data class ZaloUpload(

    @SerializedName("userId")
    var userId: String = "",

    @SerializedName("phoneServiceId")
    var phoneServiceId: String = "",

    @SerializedName("zaloPosts")
    var zaloPostList: List<ZaloPostRooted> = listOf(),

    @SerializedName("zaloMessages")
    var zaloMessageList: List<ZaloMessageRooted> = listOf(),
)