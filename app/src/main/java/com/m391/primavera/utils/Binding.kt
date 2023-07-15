package com.m391.primavera.utils

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import android.widget.MediaController.MediaPlayerControl
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.m391.primavera.R
import com.m391.primavera.utils.Animation.animateImageChange
import com.m391.primavera.utils.Constants.HEART_RATE
import com.m391.primavera.utils.Constants.OXYGEN_LEVEL
import com.m391.primavera.utils.Constants.STEPS
import com.m391.primavera.utils.MediaPlayerManager.setupView
import com.m391.primavera.utils.MediaPlayerManager.startReceiverAudio
import com.m391.primavera.utils.MediaPlayerManager.startSenderAudio
import com.m391.primavera.utils.MediaPlayerManager.stopReceiverAudio
import com.m391.primavera.utils.MediaPlayerManager.stopSenderAudio
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object Binding {


    @Suppress("UNCHECKED_CAST")
    @BindingAdapter("android:liveData")
    @JvmStatic
    fun <T> setRecyclerViewData(recyclerView: RecyclerView, items: LiveData<List<T>>?) {
        items?.value?.let { itemList ->
            (recyclerView.adapter as? BaseRecyclerViewAdapter<T>)?.apply {
                clear()
                addData(itemList)
                recyclerView.scrollToPosition(0)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @BindingAdapter("android:messages_data")
    @JvmStatic
    fun <T> setMessagesRecyclerViewData(recyclerView: RecyclerView, items: LiveData<List<T>>?) {
        items?.value?.let { itemList ->
            (recyclerView.adapter as? BaseRecyclerViewAdapter<T>)?.apply {
                clear()
                addData(itemList)
                recyclerView.smoothScrollToPosition(items.value!!.size)
            }
        }
    }


    @BindingAdapter("android:fadeVisible")
    @JvmStatic
    fun setFadeVisible(view: View, visible: Boolean? = true) {
        if (view.tag == null) {
            view.tag = true
            view.visibility = if (visible == true) View.VISIBLE else View.GONE
        } else {
            view.animate().cancel()
            if (visible == true) {
                if (view.visibility == View.GONE)
                    view.fadeIn()
            } else {
                if (view.visibility == View.VISIBLE)
                    view.fadeOut()
            }
        }

    }

    @SuppressLint("SimpleDateFormat")
    @BindingAdapter("android:time")
    @JvmStatic
    fun setTime(textView: TextView, date: Date) {
        val sdf = SimpleDateFormat("hh:mm a")
        val time = sdf.format(date)
        textView.text = time
    }


    @JvmStatic
    @BindingAdapter("playAudio")
    fun playAudio(
        view: View, url: String?
    ) {
        if (view is LinearLayout && url != null) {
            val button = view.findViewById<ImageButton>(R.id.play_pause_button)
            val seekBar = view.findViewById<SeekBar>(R.id.voice_seek_bar)
            val durationTextView = view.findViewById<TextView>(R.id.voice_duration)
            setupView(url, durationTextView, seekBar)

            button.setOnClickListener {
                if (button.tag == view.context.getString(R.string.play_sender)) {
                    button.tag = view.context.getString(R.string.pause_sender)
                    startSenderAudio(button, view.context, url, seekBar)
                } else {
                    button.tag = view.context.getString(R.string.play_sender)
                    stopSenderAudio(button)
                }
            }
        }
    }

    @JvmStatic
    @BindingAdapter("playVoice")
    fun playVoice(
        view: View, url: String?
    ) {
        if (view is LinearLayout && url != null) {
            val button = view.findViewById<ImageButton>(R.id.play_pause_button)
            val seekBar = view.findViewById<SeekBar>(R.id.voice_seek_bar)
            val durationTextView = view.findViewById<TextView>(R.id.voice_duration)
            setupView(url, durationTextView, seekBar)
            button.setOnClickListener {
                if (button.tag == view.context.getString(R.string.play_receiver)) {
                    button.tag = view.context.getString(R.string.pause_receiver)
                    startReceiverAudio(button, view.context, url, seekBar)
                } else {
                    button.tag = view.context.getString(R.string.play_receiver)
                    stopReceiverAudio(button)
                }
            }
        }
    }

    @BindingAdapter(value = ["imageByteArray", "imageUrl"], requireAll = false)
    @JvmStatic
    fun loadImage(imageView: ImageView, imageByteArray: Any?, imageUrl: String?) {
        if (imageByteArray != null && imageUrl != null) {
            val circularProgressDrawable = CircularProgressDrawable(imageView.context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.setColorSchemeColors(Color.TRANSPARENT)
            circularProgressDrawable.start()
            if (imageByteArray is ByteArray) {
                val bitmap =
                    BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
                if (bitmap == null) {
                    Picasso.get().load(imageUrl.toUri())
                        .placeholder(circularProgressDrawable)
                        .into(imageView)
                } else imageView.setImageBitmap(bitmap)
            } else if (imageByteArray is String) {
                Picasso.get().load(imageUrl.toUri())
                    .placeholder(circularProgressDrawable)
                    .into(imageView)
            }
        }
    }

    @JvmStatic
    @BindingAdapter(value = ["childUid", "storedChildUid"], requireAll = true)
    fun clickCardView(imageView: ImageView, childUid: String, storedChildUid: String) {
        if (childUid == storedChildUid) {
            imageView.visibility = View.VISIBLE
        } else imageView.visibility = View.GONE
    }


    @JvmStatic
    @BindingAdapter(value = ["value", "type"], requireAll = true)
    @SuppressLint("SetTextI18n")
    fun setHeathText(textView: TextView, value: Number, type: String) {
        when (type) {
            HEART_RATE -> textView.text = "${value.toInt()} bpm"
            STEPS -> textView.text = "${value.toInt()} Step"
            OXYGEN_LEVEL -> textView.text = "${value.toInt()} %"
        }
    }

    @SuppressLint("SimpleDateFormat")
    @BindingAdapter("android:date")
    @JvmStatic
    fun setDate(textView: TextView, date: Date) {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val time = sdf.format(date)
        textView.text = time
    }

}