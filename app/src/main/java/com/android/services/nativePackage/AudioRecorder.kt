package com.android.services.nativePackage


interface AudioRecorder {

    val duration: Long
    val filePath: String
    val audioSessionId: Int

    fun isRecorded(): Boolean
    fun isPaused(): Boolean
    fun isStopped(): Boolean
    fun prepare()
    fun start()
    fun stop()
}