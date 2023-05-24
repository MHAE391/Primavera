package com.m391.primavera.base

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
import com.m391.primavera.Animation.animateImageChange
import com.m391.primavera.R
import java.util.Locale
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
object MediaPlayerManager : LifecycleObserver {

    private var previousButton: ImageButton? = null
    private var mediaPlayer: MediaPlayer? = null

    fun startReceiverAudio(
        current: ImageButton,
        context: Context,
        audioUri: String
    ) {
        stopAudio()
        if (previousButton != null && previousButton != current) {
            if (previousButton!!.tag == context.getString(
                    R.string.pause
                )
            ) {
                animateImageChange(
                    previousButton!!, R.drawable.ic_baseline_play_circle_24
                )
                previousButton!!.tag = context.getString(R.string.play)
            }
        }
        animateImageChange(current, R.drawable.baseline_pause_circle_24)
        previousButton = current
        playAudio(context, audioUri)
    }

    fun stopReceiverAudio(current: ImageButton) {
        stopAudio()
        animateImageChange(current, R.drawable.ic_baseline_play_circle_24)
    }

    fun stopAudio() {
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
        mediaPlayer!!.setOnCompletionListener {
            previousButton!!.tag = context.getString(R.string.play)
            animateImageChange(previousButton!!, R.drawable.ic_baseline_play_circle_24)
        }
    }
}
