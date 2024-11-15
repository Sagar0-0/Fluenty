package com.sagar.fluenty.ui.managers

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

interface AudioRecorderManager {
    fun initListener(listener: AudioRecorderListener)
    fun start(context: Context)
    fun stop()
    fun cancel()
}

class AudioRecorderManagerImpl(
    private val context: Context
) : AudioRecorderManager {

    private var listener: AudioRecorderListener? = null

    private var recorder: MediaRecorder? = null

    private var outputFile: File? = null

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

    override fun start(context: Context) {
        try {
            outputFile = File(context.cacheDir, "${UUID.randomUUID()}.3gp")
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
            listener?.onErrorStarting(e)
        }
    }

    override fun stop() {
        try {
            recorder?.apply {
                stop()
                reset()
            }
            recorder = null

            outputFile?.let { listener?.onStopRecording(it) }
        } catch (e: Exception) {
            Log.e("TAG", "stop: ${e.message}")
        }
    }

    override fun cancel() {
        recorder?.apply {
            stop()
            reset()
        }
    }
}

interface AudioRecorderListener {
    fun onRecordingStarted()
    fun onErrorStarting(e: Exception)
    fun onStopRecording(file: File)
    fun onCancelRecording()
}