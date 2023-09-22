package com.android.services.util

import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import com.android.services.MyApplication.Companion.appContext
import java.io.File

object MemoryUsage {

    private var apkSize = 0.0
    private var archiveSize = 0.0
    private var docSize = 0.0

    private val docExtensions = arrayOf(
        "txt",
        "doc",
        "docx",
        "html",
        "htm",
        "odt",
        "pdf",
        "xls",
        "xlsx",
        "ods",
        "ppt",
        "pptx"
    )

    private const val apkFileExtension = "apk"
    private const val archiveFileExtension = "zip"

    fun calculateOtherFileExtensionsSize(fileSize: (String) -> Unit) {
        measureFilesSizes(Environment.getExternalStorageDirectory())
        val apkFileSize = AppUtils.formatSize(apkSize)
        val docFileSize = AppUtils.formatSize(docSize)
        val archiveFileSize = AppUtils.formatSize(archiveSize)
        fileSize("$apkFileSize $docFileSize $archiveFileSize")
    }

    private fun measureFilesSizes(directory: File) {
        directory.listFiles()?.forEach { file ->
            val size = AppUtils.convertBytesToMb(file.length().toDouble())
            when (file.isDirectory) {
                true -> measureFilesSizes(file)
                false -> {
                    when (file.extension) {
                        apkFileExtension -> apkSize += size
                        archiveFileExtension -> archiveSize += size
                        else -> {
                            if (docExtensions.contains(file.extension))
                                docSize += size
                        }
                    }
                }
            }
        }
    }

    @JvmStatic
    val freeStorage: Double
        get() {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            return (availableBlocks * blockSize / 1048576).toDouble()
        }

    @JvmStatic
    val totalStorage: Double
        get() {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            return (totalBlocks * blockSize / 1048576).toDouble()
        }

    @JvmStatic
    val imagesStorage: Double
        get() {
            var size = 0.0
            val cursor = appContext.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
                null
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val path = cursor.getString(
                        cursor
                            .getColumnIndex(MediaStore.Images.Media.DATA)
                    )
                    val data = cursor.getString(
                        cursor
                            .getColumnIndex(MediaStore.Images.Media.SIZE)
                    )
                    if (data != null) {
                        val fileSize = data.toDouble() / 1024 / 1024
                        size += fileSize
                    }
                }
                cursor.close()
            }
            return size
        }


    @JvmStatic
    val videosStorage: Double
        get() {
            var size = 0.0
            val cursor = appContext.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null,
                null
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val path = cursor.getString(
                        cursor
                            .getColumnIndex(MediaStore.Video.Media.DATA)
                    )
                    val data = cursor.getString(
                        cursor
                            .getColumnIndex(MediaStore.Video.Media.SIZE)
                    )
                    if (data != null) {
                        val fileSize = data.toDouble() / 1024 / 1024
                        size += fileSize
                    }
                }
                cursor.close()
            }
            return size
        }

    @JvmStatic
    val audioStorage: Double
        get() {
            var size = 0.0
            val cursor = appContext.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                null
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val path = cursor.getString(
                        cursor
                            .getColumnIndex(MediaStore.Audio.Media.DATA)
                    )
                    val data = cursor.getString(
                        cursor
                            .getColumnIndex(MediaStore.Audio.Media.SIZE)
                    )
                    if (data != null) {
                        val fileSize = data.toDouble() / 1024 / 1024
                        size += fileSize
                    }
                }
                cursor.close()
            }
            return size
        }

    @JvmStatic
    val otherStorage: Double
        get() {
            val stat = StatFs(Environment.getDataDirectory().path)
            val bytesAll = (stat.blockSizeLong
                    * stat.blockCountLong)
            val bytesAvailable = (stat.blockSizeLong
                    * stat.availableBlocksLong)
            return ((bytesAll - bytesAvailable) / 1048576).toDouble()
        }
}