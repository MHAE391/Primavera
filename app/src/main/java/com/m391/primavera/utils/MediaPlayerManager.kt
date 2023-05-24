package com.m391.primavera.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.m391.primavera.R
import com.m391.primavera.utils.Animation.animateImageChange
import java.util.Locale
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
object MediaPlayerManager : LifecycleObserver {

    private var previousButton: ImageButton? = null
    private var currentSeekBar: SeekBar? = null
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.myLooper()!!)
    var flags = false
    private val runnable = object : Runnable {
        override fun run() {
            if (mediaPlayer != null) {
                val currentPosition = mediaPlayer!!.currentPosition
                if (!flags) currentSeekBar?.progress = (currentPosition)
                handler.postDelayed(this, 10)
            }
        }
    }

    fun startSenderAudio(
        current: ImageButton,
        context: Context,
        audioUri: String,
        seekBar: SeekBar
    ) {
        stopAudio()
        if (previousButton != null && previousButton != current) {
            if (previousButton!!.tag == context.getString(
                    R.string.pause_sender
                )
            ) {
                animateImageChange(
                    previousButton!!, R.drawable.ic_baseline_play_arrow_reversed
                )
                previousButton!!.tag = context.getString(R.string.play_sender)
            } else if (previousButton!!.tag == context.getString(
                    R.string.pause_receiver
                )
            ) {
                animateImageChange(
                    previousButton!!, R.drawable.ic_baseline_play_arrow_24
                )
                previousButton!!.tag = context.getString(R.string.play_receiver)
            }
        }
        animateImageChange(current, R.drawable.pause_other)
        previousButton = current
        currentSeekBar = seekBar
        playAudio(context, audioUri)
        setupSeekbar(
            seekBar,
            R.drawable.ic_baseline_play_arrow_reversed,
            current,
            R.string.pause_sender
        )
    }

    fun stopSenderAudio(current: ImageButton) {
        stopAudio()
        animateImageChange(current, R.drawable.ic_baseline_play_arrow_reversed)
    }

    fun startReceiverAudio(
        current: ImageButton,
        context: Context,
        audioUri: String,
        seekBar: SeekBar
    ) {
        stopAudio()
        if (previousButton != null && previousButton != current) {
            if (previousButton!!.tag == context.getString(
                    R.string.pause_receiver
                )
            ) {
                animateImageChange(
                    previousButton!!, R.drawable.ic_baseline_play_arrow_24
                )
                previousButton!!.tag = context.getString(R.string.play_receiver)
            } else if (previousButton!!.tag == context.getString(
                    R.string.pause_sender
                )
            ) {
                animateImageChange(
                    previousButton!!, R.drawable.ic_baseline_play_arrow_reversed
                )
                previousButton!!.tag = context.getString(R.string.play_sender)
            }
        }
        animateImageChange(current, R.drawable.ic_baseline_pause_24)
        previousButton = current
        currentSeekBar = seekBar
        playAudio(context, audioUri)
        setupSeekbar(
            seekBar,
            R.drawable.ic_baseline_play_arrow_24,
            current,
            R.string.pause_receiver
        )
    }

    fun stopReceiverAudio(current: ImageButton) {
        stopAudio()
        animateImageChange(current, R.drawable.ic_baseline_play_arrow_24)
    }

    fun stopAudio() {
        handler.removeCallbacks(runnable)
        if (mediaPlayer != null) {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            }
        }
        mediaPlayer = null
    }

    private fun playAudio(context: Context, audioUri: String) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, Uri.parse(audioUri))
            prepare()
            start()
        }
    }

    fun setupView(audioUri: String, durationTextView: TextView, seekBar: SeekBar) {
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setDataSource(audioUri)
        mediaPlayer!!.prepare()
        val duration = mediaPlayer!!.duration
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) - TimeUnit.MINUTES.toSeconds(
                minutes
            )
        val durationString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        durationTextView.text = durationString
        seekBar.max = duration
        mediaPlayer = null
    }

    private fun setupSeekbar(seekBar: SeekBar, drawable: Int, button: ImageButton, string: Int) {
        if (mediaPlayer != null) {
            mediaPlayer?.seekTo(seekBar.progress)
            flags = false
            mediaPlayer?.setOnCompletionListener {
                flags = true
                seekBar.progress = 0
                animateImageChange(button, drawable)
                button.tag = button.context.getString(string)
                if (previousButton!!.tag == button.context.getString(
                        R.string.pause_receiver
                    )
                ) {
                    previousButton!!.tag = button.context.getString(R.string.play_receiver)
                } else if (previousButton!!.tag == button.context.getString(
                        R.string.pause_sender
                    )
                ) {
                    previousButton!!.tag = button.context.getString(R.string.play_sender)
                }
                handler.removeCallbacks(runnable)
            }

            handler.postDelayed(runnable, 10)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        mediaPlayer?.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // Do nothing
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    // Do nothing
                }
            })
        }
    }

}
