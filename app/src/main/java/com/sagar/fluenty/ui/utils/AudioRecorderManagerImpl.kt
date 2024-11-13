package com.sagar.fluenty.ui.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.FileOutputStream

interface AudioRecorderManager {
    fun initListener(listener: AudioRecorderListener)
    fun start(outputFile: File)
    fun stop()
}

class AudioRecorderManagerImpl(
    private val context: Context
) : AudioRecorderManager {

    private var listener: AudioRecorderListener? = null

    private var recorder: MediaRecorder? = null

    override fun initListener(listener: AudioRecorderListener) {
        this.listener = listener
    }

    private fun createMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }

    override fun start(outputFile: File) {
        try {
            createMediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(FileOutputStream(outputFile).fd)

                prepare()
                start()
                recorder = this

                listener?.onRecordingStarted()
            }
        } catch (e: Exception) {
            listener?.onErrorStarting()
        }
    }

    override fun stop() {
        recorder?.apply {
            stop()
            reset()
        }
        recorder = null

        listener?.onStopRecording()
    }
}


interface AudioRecorderListener {
    fun onRecordingStarted()
    fun onErrorStarting()
    fun onStopRecording() {}
}