package com.android.services.services.callRecord

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.android.services.interfaces.IBackgroundProcessing
import com.android.services.models.CallRecord
import com.android.services.nativePackage.AudioRecorder
import com.android.services.util.Mp3LameRecorder
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class CallRecordProcessingBaseI(val context: Context) : IBackgroundProcessing {

    internal val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    internal var disposable: Disposable? = null
    internal var callRecord: CallRecord? = null
    internal var mFilePath: String? = null
    internal var mRecordingStartTime: Long = 0L
    protected var mLastCallId = 0
    internal var mp3LameRecorder: Mp3LameRecorder? = null
    internal var isEventMarkedAsReceived=false
    internal var mHandler: Handler = Handler(Looper.getMainLooper())

    companion object {
        internal const val TAG = "CallRecordProcessingBase"
        internal const val INITIAL_DELAY = 30 * 1000L
        internal const val PERIODIC_INTERVAL = 30 * 1000L
        internal const val CALL_RECORD_NOTIFICATION_ID = 110
    }

    object IntentKey {
        internal const val KEY_CALL_RECORD = "KEY_CALL_RECORD"
    }

    abstract fun initialize()
    abstract fun parseIntent(intent: Intent?)
    abstract fun createOutputFilePath(): String
    abstract fun startRecording()
    abstract fun startCommand()
    abstract suspend fun stopRecording()
    abstract fun onServiceDestroy()


    override fun onCreate() {
        initialize()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startCommand()
        parseIntent(intent)
        return Service.START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        onServiceDestroy()
    }
}
