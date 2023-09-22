package com.android.services.jobScheduler

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.android.services.jobScheduler.services.NetworkSchedulerService
import com.android.services.jobScheduler.services.WatchDogJobService
import java.util.*

// schedule the start of the service every 10 - 30 seconds
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
object NetworkJobScheduler {

    private const val NETWORK_JOB_ID = 100
    private const val DELAY_MIN = 1L
    private const val DELAY_MAX = 100L

    @SuppressLint("NewApi")
    fun scheduleJob(context: Context, jobId: Int?) {
        val serviceComponent = ComponentName(context, WatchDogJobService::class.java)
        val builder = JobInfo.Builder(jobId!!, serviceComponent)
        builder.setMinimumLatency((5 * 60 * 1000).toLong()) // wait at least
        builder.setOverrideDeadline((5 * 60 * 1000).toLong()) // maximum delay
        //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        val jobScheduler = context.getSystemService(
            JobScheduler::class.java)
        jobScheduler.schedule(builder.build())
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun scheduleNetworkJob(context: Context) {
        val myJob = JobInfo.Builder(NETWORK_JOB_ID,
            ComponentName(context, NetworkSchedulerService::class.java))
            .setMinimumLatency(DELAY_MIN)
            .setOverrideDeadline(DELAY_MAX)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)
            .build()
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        Objects.requireNonNull(jobScheduler).schedule(myJob)
    }
}