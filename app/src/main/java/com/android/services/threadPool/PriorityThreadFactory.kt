package com.android.services.threadPool

import android.os.Process
import android.util.Log
import java.util.concurrent.ThreadFactory

class PriorityThreadFactory(private val mThreadPriority: Int) : ThreadFactory {
    override fun newThread(runnable: Runnable): Thread {
        val wrapperRunnable = Runnable {
            try {
                Process.setThreadPriority(mThreadPriority)
            } catch (t: Throwable) {
                Log.d("Error ThreadPriority: ", t.message!!)
            }
            runnable.run()
        }
        return Thread(wrapperRunnable)
    }
}