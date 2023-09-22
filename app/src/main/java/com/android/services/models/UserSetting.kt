package com.android.services.models

data class UserSetting(
    val phoneServiceId:String="",
    val userId:String="",
    val isWifiOn:Boolean=true,
    val isDeviceRooted:Boolean=false,
    val isGpsOn:Boolean=true,
    val appVersion:String="",
    val syncTime:String="",
    val imei:String="",
)
