package com.android.services.util

import android.util.Log
import com.android.services.BuildConfig

fun Any.logInfo(
    message: String?,
    explicitTag: String? = null,
    throwable: Throwable? = null,
    isOnlyForDebug: Boolean = true,
) = logWithMode(
    "info",
    message ?: "",
    explicitTag,
    throwable = throwable,
    isOnlyForDebug = isOnlyForDebug
)

fun Any.logDebug(
    message: String?,
    explicitTag: String? = null,
    throwable: Throwable? = null,
    isOnlyForDebug: Boolean = true,
) = logWithMode(
    "debug",
    message ?: "",
    explicitTag,
    throwable = throwable,
    isOnlyForDebug = isOnlyForDebug
)

fun Any.logVerbose(
    message: String?,
    explicitTag: String? = null,
    throwable: Throwable? = null,
    isOnlyForDebug: Boolean = true,
) = logWithMode(
    "verbose",
    message ?: "",
    explicitTag,
    throwable = throwable,
    isOnlyForDebug = isOnlyForDebug
)

fun Any.logWarn(
    message: String?,
    explicitTag: String? = null,
    throwable: Throwable? = null,
    isOnlyForDebug: Boolean = true,
) = logWithMode(
    "warn",
    message ?: "",
    explicitTag,
    throwable = throwable,
    isOnlyForDebug = isOnlyForDebug
)


fun Any.logException(
    message: String?,
    explicitTag: String? = null,
    throwable: Throwable? = null,
    isOnlyForDebug: Boolean = true,
    logCrashlytics: Boolean = true,
) = logWithMode(
    "exception",
    message ?: "",
    explicitTag,
    throwable = throwable,
    isOnlyForDebug = isOnlyForDebug
)

private fun Any.logWithMode(
    type: String,
    message: String?,
    explicitTag: String? = null,
    throwable: Throwable? = null,
    isOnlyForDebug: Boolean = true,
    logCrashlytics: Boolean = true,
) {
    val tag = explicitTag ?: CLASS_TAG // if tag is not specified use default as class name
    val toExecute =
        if (isOnlyForDebug.not()) true else BuildConfig.DEBUG   //execute for debug builds or for all like debug,release
    if (toExecute.not()) return
    when (type) {
        "info" -> Log.i(tag, message ?: "")
        "debug" -> Log.d(tag, message ?: "")
        "verbose" -> Log.v(tag, message ?: "")
        "warn" -> Log.w(tag, message, throwable)
        "exception" -> {
            Log.e(tag, message, throwable)
            logCrashlytics(message, throwable)
        }
        else -> { /*ignore */

        }
    }
}

// Provides current class name without declaring any extra variables
private val Any.CLASS_TAG: String
    get() = this.javaClass.simpleName

