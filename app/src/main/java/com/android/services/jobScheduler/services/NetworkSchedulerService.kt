package com.android.services.jobScheduler.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import com.android.services.receiver.ConnectivityChangeReceiver
import com.android.services.util.logVerbose

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class NetworkSchedulerService : JobService() {

    private var mConnectivityReceiver: ConnectivityChangeReceiver? = null

    override fun onCreate() {
        super.onCreate()
        logVerbose(TAG, " Service created")
        mConnectivityReceiver = ConnectivityChangeReceiver()
    }

    /**
     * When the app's NetworkConnectionActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCallback()"
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        logVerbose(TAG, "onStartCommand")
        return START_NOT_STICKY
    }

    override fun onStartJob(params: JobParameters): Boolean {
        logVerbose(TAG, " onStartJob$mConnectivityReceiver")
        registerReceiver(
            mConnectivityReceiver,
            IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        )
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        logVerbose(TAG, " onStopJob")
        unregisterReceiver(mConnectivityReceiver)
        return true
    }

    companion object {
        private val TAG = NetworkSchedulerService::class.java.simpleName
    }
}