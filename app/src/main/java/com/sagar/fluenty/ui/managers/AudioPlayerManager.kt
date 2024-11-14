package com.sagar.fluenty.ui.managers

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import java.io.File

interface AudioPlayerManager {
    fun initListener(listener: AudioPlayerListener)
    fun play(file: File)
    fun stop()
}

class AudioPlayerManagerImpl(
    private val context: Context
) : AudioPlayerManager {

    private var listener: AudioPlayerListener? = null
    override fun initListener(listener: AudioPlayerListener) {
        this.listener = listener
    }

    private var player: MediaPlayer? = null

    private fun createMediaPlayer(file: File): MediaPlayer {
        return MediaPlayer.create(context, file.toUri())
    }

    override fun play(file: File) {
        try {
            createMediaPlayer(file).apply {
                start()
                player = this

                listener?.onPlayerStarted()

                player!!.setOnCompletionListener {
                    listener?.onCompletePlaying()
                    this@AudioPlayerManagerImpl.stop()
                }
            }
        } catch (e: Exception) {
            listener?.onErrorPlaying()
        }
    }

    override fun stop() {
        player?.apply {
            stop()
            release()
        }
        player = null

        listener?.onStopPlayer()
    }

}

interface AudioPlayerListener {
    fun onPlayerStarted()
    fun onErrorPlaying()
    fun onStopPlayer()
    fun onCompletePlaying()
}