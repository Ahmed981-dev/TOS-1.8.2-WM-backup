package com.android.services.interfaces

interface LogsCollector {
    @Throws(Exception::class)
    fun uploadLogs()
}