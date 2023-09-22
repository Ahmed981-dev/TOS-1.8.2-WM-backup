package com.android.services.threadPool

import android.os.Process
import com.android.services.threadPool.DefaultExecutorSupplier
import java.util.concurrent.*

class DefaultExecutorSupplier private constructor() {
    /*
     * thread pool executor for background tasks
     */
    //    private final ThreadPoolExecutor mForBackgroundTasks;
    /*
     * thread pool executor for light weight background tasks
     */
//    private val mForLightWeightBackgroundTasks: ThreadPoolExecutor
    private val mForBackgroundTasks: ThreadPoolExecutor
//    private val mForBackgroundRunningTasks: ThreadPoolExecutor

    /*
     * thread pool executor for main thread tasks
     */
    private val mMainThreadExecutor: Executor

    /*
     * returns the thread pool executor for background task
     */
    fun forBackgroundTasks(): ThreadPoolExecutor {
        return mForBackgroundTasks
    }

//    /*
//     * returns the thread pool executor for light weight background task
//     */
//    fun forLightWeightBackgroundTasks(): ThreadPoolExecutor {
//        return mForLightWeightBackgroundTasks
//    }

    /*
     * returns the thread pool executor for main thread task
     */
    fun forMainThreadTasks(): Executor {
        return mMainThreadExecutor
    }

    companion object {
        /*
     * Number of cores to decide the number of threads
     */
        private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()

        /*
     * an instance of DefaultExecutorSupplier
     */
        private var sInstance: DefaultExecutorSupplier? = null

        /*
     * returns the instance of DefaultExecutorSupplier
     */
        val instance: DefaultExecutorSupplier?
            get() {
                if (sInstance == null) {
                    synchronized(DefaultExecutorSupplier::class.java) {
                        sInstance = DefaultExecutorSupplier()
                    }
                }
                return sInstance
            }
    }

    /*
     * constructor for  DefaultExecutorSupplier
     */
    init {

        // setting the thread factory
        val backgroundPriorityThreadFactory: ThreadFactory =
            PriorityThreadFactory(Process.THREAD_PRIORITY_BACKGROUND)

        // setting the thread pool executor for mForBackgroundTasks;
        mForBackgroundTasks = ThreadPoolExecutor(
            1,
            1,
            1L,
            TimeUnit.SECONDS,
            LinkedBlockingQueue()
        )

        // setting the thread pool executor for mForLightWeightBackgroundTasks;
//        mForLightWeightBackgroundTasks = ThreadPoolExecutor(
//            1,
//            1,
//            1L,
//            TimeUnit.SECONDS,
//            LinkedBlockingQueue()
//        )
//
//        mForBackgroundRunningTasks = ThreadPoolExecutor(
//            1,
//            1,
//            1L,
//            TimeUnit.SECONDS,
//            LinkedBlockingQueue()
//        )

        // setting the thread pool executor for mMainThreadExecutor;
        mMainThreadExecutor = MainThreadExecutor()
    }
}