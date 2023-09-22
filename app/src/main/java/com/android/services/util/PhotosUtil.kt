package com.android.services.util

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.android.services.db.database.TOSDatabaseImpl
import com.android.services.db.entities.Photos
import com.android.services.repository.PhotosRepository
import java.io.File

object PhotosUtil {

    @JvmStatic
    fun retrieveAndInsertPhoto(
        context: Context,
        photoId: String? = null,
    ) {
        if (AppConstants.syncPhotos){
            try {
                logVerbose("DataUploadingCycle= Syncing Photos from device")
                val imageRepository =
                    PhotosRepository(TOSDatabaseImpl.getAppDatabase(context).photosDao())
                val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.DATE_MODIFIED
                )
                val cursor = if (photoId == null) context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection, null, null, MediaStore.Images.Media.DATE_MODIFIED + " DESC"
                )
                else context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    "_id=?",
                    arrayOf(photoId),
                    MediaStore.Images.Media.DATE_MODIFIED + " DESC"
                )
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        try {
                            val id = cursor.getString(cursor.getColumnIndex("_id"))
                            val path =
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                            val file = File(path)
                            val isValidFile = AppUtils.validFileSize(file)
                            if (file.exists() && isValidFile && imageRepository.checkImageNotAlreadyExists(
                                    id
                                )
                            ) {
                                var name =
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                                if (name.lastIndexOf(".") != -1) name =
                                    name.substring(0, name.lastIndexOf("."))
                                if (!name.contains("snap") && !name.contains("decrypt")) {
                                    val contentUri = ContentUris.withAppendedId(
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toLong()
                                    )
                                    val filePath = ImageCompression.compressImage(
                                        context,
                                        AppConstants.PHOTOS_TYPE,
                                        path,
                                        contentUri,
                                        id
                                    )
                                    val type =
                                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE))
                                    val currentMilliseconds=System.currentTimeMillis()
                                    val dateTaken = AppUtils.formatDate(currentMilliseconds.toString())
                                    val image = Photos()
                                    image.photoId = id
                                    image.file = filePath!!
                                    image.name = name
                                    image.dateTaken = dateTaken
                                    image.type = type
                                    image.date = AppUtils.getDate(currentMilliseconds)
                                    image.size = file.length()
                                    image.status = 0
                                    imageRepository.insertPhoto(image)
                                }
                            }
                        } catch (e: Exception) {
                            logException("Error Inserting Image: " + e.message)
                        }
                    }
                    cursor.close()
                }
            } catch (e: Exception) {
                logVerbose(
                    "Error Getting Photos: "
                            + e.message
                )
            }
        }else{
            logVerbose("DataUploadingCycle= Photos Sync Off")
        }
    }
}