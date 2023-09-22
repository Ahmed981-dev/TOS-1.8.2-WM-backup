package com.android.services.threadPool

import com.android.services.threadPool.DefaultExecutorSupplier
import com.android.services.threadPool.PriorityThreadFactory
import com.android.services.threadPool.PriorityThreadPoolExecutor.PriorityFutureTask
import com.android.services.threadPool.PriorityRunnable
import java.util.concurrent.*

class PriorityThreadPoolExecutor(
    corePoolSize: Int, maximumPoolSize: Int, keepAliveTime: Long,
    unit: TimeUnit?, threadFactory: ThreadFactory?
) : ThreadPoolExecutor(corePoolSize,
    maximumPoolSize,
    keepAliveTime,
    unit,
    PriorityBlockingQueue(),
    threadFactory) {
    override fun submit(task: Runnable): Future<*> {
        val futureTask = PriorityFutureTask(task as PriorityRunnable)
        execute(futureTask)
        return futureTask
    }

    private class PriorityFutureTask(private val priorityRunnable: PriorityRunnable) :
        FutureTask<PriorityRunnable?>(
            priorityRunnable, null), Comparable<PriorityFutureTask> {
        /*
         * compareTo() method is defined in interface java.lang.Comparable and it is used
         * to implement natural sorting on java classes. natural sorting means the the sort
         * order which naturally applies on object e.g. lexical order for String, numeric
         * order for Integer or Sorting employee by there ID etc. most of the java core
         * classes including String and Integer implements CompareTo() method and provide
         * natural sorting.
         */
        override fun compareTo(other: PriorityFutureTask): Int {
            val p1 = priorityRunnable.priority
            val p2 = other.priorityRunnable.priority
            return p2.ordinal - p1.ordinal
        }
    }
}