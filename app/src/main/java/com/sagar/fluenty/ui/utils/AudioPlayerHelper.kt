package com.sagar.fluenty.ui.utils

import android.media.MediaPlayer


object AudioPlayerHelper {
    private var mPlayer: MediaPlayer? = null

    private var listener: Listener? = null
    fun initListener(listener: Listener) {
        this.listener = listener
    }

    fun startPlaying(fileName: String) {
        mPlayer = MediaPlayer()
        try {
            mPlayer!!.apply {
                setDataSource(fileName)
                prepare()
                start()
                listener?.onPlayerStarted()
            }
        } catch (e: Exception) {
            listener?.onErrorPlaying()
        }
    }

    fun pause() {
        mPlayer?.pause()
        listener?.onPausePlayer()
    }

    fun resume() {
        mPlayer?.start()
        listener?.onResumePlayer()
    }

    fun stopRecording() {
        mPlayer?.stop()
        mPlayer?.release()
        mPlayer = null
        listener?.onStopPlayer()
    }

    interface Listener {
        fun onPlayerStarted()
        fun onErrorPlaying()
        fun onPausePlayer() {}
        fun onResumePlayer() {}
        fun onStopPlayer() {}
    }
}