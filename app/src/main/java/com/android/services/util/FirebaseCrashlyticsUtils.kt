package com.android.services.util

import com.google.firebase.crashlytics.FirebaseCrashlytics

fun Any.logCrashlytics(message: String? = null, throwable: Throwable? = null) {
    message?.let {
        FirebaseCrashlytics.getInstance().log(it)
    }
    throwable?.let {
        FirebaseCrashlytics.getInstance().recordException(it)
    }
}