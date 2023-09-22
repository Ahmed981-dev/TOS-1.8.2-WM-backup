package com.android.services.jobScheduler.services

import android.annotation.TargetApi
import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import android.provider.CallLog
import android.widget.Toast
import com.android.services.jobScheduler.ObserverJobScheduler
import com.android.services.util.logVerbose
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread

@TargetApi(Build.VERSION_CODES.N)
class CallObserverJobService : JobService() {

    override fun onStartJob(params: JobParameters): Boolean {
        val context = baseContext
        doAsync {
            if (params.triggeredContentAuthorities != null &&
                params.triggeredContentUris != null
            ) {
                for (uri in params.triggeredContentUris!!) {
                    try {
                        val uriString = uri.toString()
                        val authority = uri.authority!!
                        if (uriString.contains(CallLog.Calls.CONTENT_URI.toString()) || authority == "call_log") {
//                            onCallUriChanged()
                        }
                    } catch (e: Exception) {
                        logVerbose("Observer Error: = " + e.message)
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

    private fun onCallUriChanged() {
        runOnUiThread {
            Toast.makeText(this, "Call Uri Changed", Toast.LENGTH_LONG).show()
        }
    }
}