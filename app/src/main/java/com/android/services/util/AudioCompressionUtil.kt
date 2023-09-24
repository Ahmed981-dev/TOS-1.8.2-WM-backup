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
    fun compressAudio(context: Context, sourceFilePath: String, destFile: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                when (val rc =
                    FFmpeg.execute("-y -i $sourceFilePath -ar 44100 -ac 2 -ab 20k -c:v mpeg4 $destFile")) {
                    Config.RETURN_CODE_SUCCESS -> {
                        logVerbose(
                            "Command execution completed successfully.",
                            AppConstants.MIC_BUG_TYPE
                        )
                        destFile
                    }

                    Config.RETURN_CODE_CANCEL -> {
                        logVerbose(
                            "Command execution cancelled by user.",
                            AppConstants.MIC_BUG_TYPE
                        )
                        null
                    }

                    else -> {
                        logVerbose(
                            String.format(
                                "Command execution failed with rc=%d and the output below.",
                                rc
                            ), AppConstants.MIC_BUG_TYPE
                        )
                        Config.printLastCommandOutput(Log.INFO)
                        null
                    }
                }
            }catch (ex:Exception){

            }
        }
    }
}