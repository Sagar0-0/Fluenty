package com.sagar.fluenty.ui.utils

import android.media.MediaRecorder
import android.os.Environment
import java.util.UUID


object AudioRecorderHelper {
    private var mRecorder: MediaRecorder = MediaRecorder().apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
    }

    private var mFileName: String? = null

    private var listener: Listener? = null
    fun initListener(listener: Listener) {
        this.listener = listener
    }

    fun startRecording() {
        mFileName = Environment.getExternalStorageDirectory().absolutePath
        mFileName += "/${UUID.randomUUID()}.3gp"

        mRecorder.apply {
            setOutputFile(mFileName)
            try {
                prepare()
                start()
                listener?.onRecordingStarted()
            } catch (e: Exception) {
                listener?.onErrorStarting()
            }
        }
    }

    fun stopRecording() {
        mRecorder.stop()
        listener?.onStopRecording()
    }

    fun destroy() {
        mRecorder.release()
    }

    interface Listener {
        fun onRecordingStarted()
        fun onErrorStarting()
        fun onStopRecording() {}
    }
}