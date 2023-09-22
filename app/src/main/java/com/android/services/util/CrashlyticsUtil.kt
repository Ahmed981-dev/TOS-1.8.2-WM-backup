package com.android.services.util

import com.google.firebase.crashlytics.FirebaseCrashlytics

object CrashlyticsUtil {

    fun setUserId(id: String?) {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setUserId(id!!)
    }

    fun addCrashlyticsLog(log: String?) {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log(log!!)
    }

    @JvmStatic
    fun addCrashlyticsException(throwable: Throwable?) {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.recordException(throwable!!)
    }
}