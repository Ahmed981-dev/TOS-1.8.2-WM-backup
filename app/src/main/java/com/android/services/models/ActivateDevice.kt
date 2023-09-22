package com.android.services.models

data class ActivateDevice(
    val phoneServiceModel: String = "",
    val phoneServiceOs: String = "",
    val phoneServiceSimId: String = "",
    val phoneServiceImeiNo: String = "",
    var phoneServiceCode: String = "",
    val phoneServiceDevice: String = "",
    val phoneServiceVersion: String = "",
    val carrierName: String = "",
    val mccMnc: String = "",
    val networkType:String="",
    val deviceIdentifier:String=""
)