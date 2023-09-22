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

object VideoCompressionUtil {

    /**
     * This method provides with the utility to Compress the Video File
     * @param sourceFile Source File
     * @param destFile Destination File
     */
    fun compressVideo(context: Context, sourceFilePath: String, destFile: String) {
//        try {
//            val sourceFile= File(sourceFilePath)
//            val quality= if(sourceFile.exists() && sourceFile.sizeInMb >20){
//                VideoQuality.VERY_LOW
//            }else{
//                VideoQuality.LOW
//            }
//            VideoCompressor.start(
//                sourceFilePath,
//                destFile,
//                object : CompressionListener {
//                    override fun onProgress(percent: Float) {
//                    }
//
//                    override fun onStart() {
//
//                    }
//
//                    override fun onSuccess() {
//                        GlobalScope.launch(Dispatchers.Default) {
//                            reNameFile(context, sourceFilePath, destFile)
//                            updateCompressionStatus(context, sourceFilePath)
//                        }
//                    }
//
//                    override fun onFailure(failureMessage: String) {
//
//                    }
//
//                    override fun onCancelled() {
//                        logVerbose("Compression has been cancelled for $sourceFilePath")
//                    }
//                },
//                quality,
//                isMinBitRateEnabled = false,
//                keepOriginalResolution = false,
//            )
//        } catch (exp: Exception) {
//            logVerbose("Error While compressing $sourceFilePath = ${exp.message}")
//        }
        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (AppConstants.osGreaterThanEqualToNougat) {
                    val destFilePath = sourceFilePath.replace(".src", ".mp4")
                    // This command will compress the file 67%
                    logVerbose("size before compression = ${File(sourceFilePath).sizeInKb}")
                    val command = arrayOf(
                        "-y",
                        "-i",
                        sourceFilePath,
                        "-s",
                        "360x640",
                        "-vcodec",
                        "h264",
                        "-crf",
                        "28",
                        "-b:v",
                        "150k",
                        "-b:a",
                        "48000",
                        "-ac",
                        "2",
                        "-ar",
                        "22050",
                        destFilePath
                    )
                    when (val rc = FFmpeg.execute(command)) {
                        Config.RETURN_CODE_SUCCESS -> {
                            logVerbose(
                                "Command execution completed successfully.",
                                AppConstants.VIDEO_BUG_TYPE
                            )
                            logVerbose("size after compression = ${File(destFile).sizeInKb}")
                            reNameFile(context, sourceFilePath, destFilePath)
                            updateCompressionStatus(context, sourceFilePath)
                        }
                        Config.RETURN_CODE_CANCEL -> {
                            logVerbose(
                                "Command execution cancelled by user.",
                                AppConstants.VIDEO_BUG_TYPE
                            )
                            AppUtils.deleteFile(context,destFilePath)
                            updateCompressionStatus(context, sourceFilePath)

                        }
                        else -> {
                            logVerbose(
                                String.format(
                                    "Command execution failed with rc=%d and the output below.",
                                    rc
                                ), AppConstants.VIDEO_BUG_TYPE
                            )
                            Config.printLastCommandOutput(Log.INFO)
                            AppUtils.deleteFile(context,destFilePath)
                            updateCompressionStatus(context, sourceFilePath)
                        }
                    }

                } else {
                    updateCompressionStatus(context, sourceFilePath)
                }
            } catch (e: Exception) {
                updateCompressionStatus(context, sourceFilePath)
            }
        }
    }

    private suspend fun reNameFile(context: Context, sourceFile: String, destFile: String) {
        withContext(Dispatchers.IO) {
            AppUtils.reNameSourceFileWithDestFile(
                context,
                sourceFile,
                destFile,
                AppConstants.SCREEN_RECORDING_TYPE
            )
        }
    }

    suspend fun updateCompressionStatus(context: Context, sourceFile: String) {
        withContext(Dispatchers.IO) {
            InjectorUtils.provideScreenRecordingRepository(context)
                .updateScreenRecordingCompressedStatus(sourceFile)
        }
    }

}