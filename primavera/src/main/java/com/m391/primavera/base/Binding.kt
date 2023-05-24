package com.m391.primavera.base

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.m391.primavera.Animation.animateImageChange
import com.m391.primavera.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object Binding {

    @Suppress("UNCHECKED_CAST")
    @BindingAdapter("android:messages_data")
    @JvmStatic
    fun <T> setMessagesRecyclerViewData(recyclerView: RecyclerView, items: LiveData<List<T>>?) {
        items?.value?.let { itemList ->
            (recyclerView.adapter as? BaseRecyclerViewAdapter<T>)?.apply {
                clear()
                addData(itemList)
                recyclerView.scrollToPosition(items.value!!.size - 1)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("playAudio")
    fun playAudio(
        view: View, url: String?
    ) {
        if (view is RelativeLayout && url != null) {
            val button = view.findViewById<ImageButton>(R.id.play_pause_button)

            button.setOnClickListener {
                if (it.tag == view.context.getString(R.string.play)) {
                    button.tag = view.context.getString(R.string.pause)
                    MediaPlayerManager.startReceiverAudio(button, view.context, url)
                } else {
                    button.tag = view.context.getString(R.string.play)
                    MediaPlayerManager.stopReceiverAudio(button)
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @BindingAdapter("time")
    @JvmStatic
    fun setTime(textView: TextView, date: Date) {
        val sdf = SimpleDateFormat("hh:mm a")
        val time = sdf.format(date)
        textView.text = time
    }

}