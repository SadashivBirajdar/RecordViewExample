package com.example.birajdar.recordviewexample

import android.content.Context
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.widget.SeekBar
import android.widget.Toast

class AudioPlayerHelper(private val context: Context) : Runnable, SeekBar.OnSeekBarChangeListener {

    private var mediaPlayer: MediaPlayer? = MediaPlayer()
    private var wasPlaying = false
    private var isPaused = false
    private lateinit var audioListener: AudioPlayerListener

    init {
        if (context is AudioPlayerListener) {
            audioListener = context
        }
    }

    interface AudioPlayerListener {
        fun updateSeekBar(progress: Int)
        fun updateAudioProgress(progress: Long)
        fun updatePlayButton(drawable: Drawable?)
        fun setSeekBarMax(duration: Int)
    }

    //onCompletionListener method
    private var completionListener = MediaPlayer.OnCompletionListener {
        clearMediaPlayer()
    }

    fun clearMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
        audioListener.updateSeekBar(0)
        audioListener.updatePlayButton(ContextCompat.getDrawable(context, android.R.drawable.ic_media_play))
    }

    private fun pausePlayer() {
        mediaPlayer!!.pause()
        isPaused = true
    }

    private fun startPlayer() {
        mediaPlayer!!.start()
        isPaused = false
        Thread(this).start()
    }

    override fun run() {

        var currentPosition = mediaPlayer!!.currentPosition
        val total = mediaPlayer!!.duration
        while (mediaPlayer != null && mediaPlayer!!.isPlaying && currentPosition < total) {
            try {
                Thread.sleep(1000)
                currentPosition = mediaPlayer!!.currentPosition
            } catch (e: InterruptedException) {
                return
            } catch (e: Exception) {
                return
            }

            audioListener.updateSeekBar(currentPosition)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        audioListener.updateAudioProgress(0)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromTouch: Boolean) {
        audioListener.updateAudioProgress(progress.toLong())
        mediaPlayer?.let {
            val isFileEnded = progress / 1000 == (it.duration / 1000)
            if (!it.isPlaying && isFileEnded) {
                clearMediaPlayer()
            }
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.seekTo(seekBar.progress)
        }
    }

    fun playSong(uri: Uri?) {
        if (uri != null) {
            try {
                if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                    pausePlayer()
                    wasPlaying = true
                    audioListener.updatePlayButton(
                        ContextCompat.getDrawable(context, R.drawable.ic_media_play)
                    )
                } else if (mediaPlayer != null && isPaused) {
                    startPlayer()
                    wasPlaying = true
                    audioListener.updatePlayButton(
                        ContextCompat.getDrawable(context, R.drawable.ic_media_pause)
                    )
                }

                if (!wasPlaying) {

                    if (mediaPlayer == null) {
                        mediaPlayer = MediaPlayer()
                    }

                    audioListener.updatePlayButton(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_media_pause
                        )
                    )

                    mediaPlayer!!.setDataSource(context, uri)

                    mediaPlayer!!.prepare()
                    mediaPlayer!!.setVolume(0.5f, 0.5f)
                    mediaPlayer!!.isLooping = false
                    audioListener.updateSeekBar(0)
                    audioListener.setSeekBarMax(mediaPlayer!!.duration)
                    mediaPlayer!!.setOnCompletionListener(completionListener)
                    startPlayer()
                }
                wasPlaying = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(context, "Empty Audio", Toast.LENGTH_SHORT).show()
        }
    }
}
