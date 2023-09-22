package com.android.services.util

import android.content.Context
import com.android.services.db.database.TOSDatabaseImpl
import com.android.services.repository.*

object InjectorUtils {

    fun provideTextAlertRepository(context: Context): TextAlertRepository =
        TextAlertRepository(TOSDatabaseImpl.getAppDatabase(context).textAlertDao())

    fun provideScreenRecordingRepository(context: Context): ScreenRecordingRepository =
        ScreenRecordingRepository(TOSDatabaseImpl.getAppDatabase(context).screenRecordingDao())

    fun provideConnectedNetworkRepository(context: Context): ConnectedNetworkRepository =
        ConnectedNetworkRepository(TOSDatabaseImpl.getAppDatabase(context).connectedNetworkDao())

    fun providePushStatusRepository(context: Context): PushStatusRepository =
        PushStatusRepository(TOSDatabaseImpl.getAppDatabase(context).pushStatusDao())

    fun provideCallLogRepository(context: Context): CallLogRepository =
        CallLogRepository(TOSDatabaseImpl.getAppDatabase(context).callLogDao())

    fun provideWebSiteRepository(context: Context): WebSiteRepository =
        WebSiteRepository(TOSDatabaseImpl.getAppDatabase(context).webSiteDao())

    fun provideInstalledAppsRepository(context: Context): InstalledAppRepository =
        InstalledAppRepository(TOSDatabaseImpl.getAppDatabase(context).installedAppDao())

    fun provideBlockedAppsRepository(context: Context): BlockedAppRepository =
        BlockedAppRepository(TOSDatabaseImpl.getAppDatabase(context).blockedAppDao())

    fun provideRestrictedCallRepository(context: Context): RestrictedCallRepository =
        RestrictedCallRepository(TOSDatabaseImpl.getAppDatabase(context).restrictedCallDao())

    fun provideScreenLimitRepository(context: Context): ScreenLimitRepository =
        ScreenLimitRepository(TOSDatabaseImpl.getAppDatabase(context).screenLimitDao())

    // App Limit Repository
    fun provideAppLimitRepository(context: Context): AppLimitRepository =
        AppLimitRepository(TOSDatabaseImpl.getAppDatabase(context).appLimitDao())

    // Sms Log Repository
    fun provideSmsLogRepository(context: Context): SmsLogRepository =
        SmsLogRepository(TOSDatabaseImpl.getAppDatabase(context).smsLogDao())

    // GeoFence Event Repository
    fun provideGeoFenceEventRepository(context: Context): GeoFenceEventRepository =
        GeoFenceEventRepository(TOSDatabaseImpl.getAppDatabase(context).geoFenceEventDao())

    // TextAlerts Event Repository
    fun provideTextAlertEventRepository(context: Context): TextAlertEventRepository =
        TextAlertEventRepository(TOSDatabaseImpl.getAppDatabase(context).textAlertEventsDao())
}