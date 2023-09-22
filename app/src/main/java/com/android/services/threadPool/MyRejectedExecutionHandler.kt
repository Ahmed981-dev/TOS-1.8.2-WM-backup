package com.android.services.threadPool

import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadPoolExecutor

class MyRejectedExecutionHandler : RejectedExecutionHandler {
    override fun rejectedExecution(r: Runnable, executor: ThreadPoolExecutor) {
        println("$r is rejected")
    }
}