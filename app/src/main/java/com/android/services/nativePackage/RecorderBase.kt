package com.android.services.nativePackage

/**
 * Created by Viktor Degtyarev on 16.10.17
 * E-mail: viktor@degtyarev.biz
 */
abstract class RecorderBase : AudioRecorder {
    protected enum class State {
        RECORD, PAUSE, STOP
    }

    protected var startTimeRecording: Long = -1L
    override val duration: Long
        get() {
            if (startTimeRecording == -1L) return 0
            return System.currentTimeMillis() - startTimeRecording
        }

    protected var state = State.STOP

    @Throws(RecorderException::class)
    abstract override fun prepare()

    @Throws(RecorderException::class)
    abstract override fun start()

    abstract override fun stop()

    override fun isRecorded() = state == State.RECORD

    override fun isPaused() = state == State.PAUSE

    override fun isStopped() = state == State.STOP

    class RecorderException : Exception {
        val codeError: Int

        constructor(message: String, codeError: Int) : super(message) {
            this.codeError = codeError
        }

        constructor(message: String, throwable: Throwable, codeError: Int) : super(message,
            throwable) {
            this.codeError = codeError
        }
    }

    object CodeError {
        const val ERROR_BUFFER_SIZE = 1
        const val ERROR_BUFFER_SIZE_STEREO = 2
        const val ERROR_INITIALIZE_RECORDER = 3
    }

}
