package com.m391.primavera.utils

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.firebase.storage.FirebaseStorage
import com.m391.primavera.R
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.user.father.teacher.TeacherProfileViewModel
import com.m391.primavera.utils.Animation.animateImageChange
import com.m391.primavera.utils.models.ServerMessageModel
import com.m391.primavera.utils.models.ServerTeacherModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.runBlocking
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
                recyclerView.scrollToPosition(items.value!!.size - 1)
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
    @BindingAdapter("time")
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
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepare()
            val duration = mediaPlayer.duration
            val minutes = TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
            val seconds =
                TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) - TimeUnit.MINUTES.toSeconds(
                    minutes
                )
            val durationString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            durationTextView.text = durationString
            seekBar.max = duration
            val handler = Handler(Looper.myLooper()!!)
            var flags: Boolean = false
            handler.postDelayed(object : Runnable {
                override fun run() {
                    val currentPosition = mediaPlayer.currentPosition
                    if (!flags) seekBar.progress = (currentPosition)
                    handler.postDelayed(this, 100)
                }
            }, 100)


            mediaPlayer.setOnCompletionListener {
                flags = true
                seekBar.progress = 0
                animateImageChange(button, R.drawable.ic_baseline_play_arrow_reversed)
                button.tag = view.context.getString(R.string.play)
            }
            button.setOnClickListener {
                if (it.tag == view.context.getString(R.string.play)) {
                    animateImageChange(button, R.drawable.pause_other)
                    it.tag = view.context.getString(R.string.pause)
                    mediaPlayer.start()
                    flags = false
                } else {
                    animateImageChange(button, R.drawable.ic_baseline_play_arrow_reversed)
                    it.tag = view.context.getString(R.string.play)
                    mediaPlayer.pause()
                }
            }
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress)
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

    @JvmStatic
    @BindingAdapter("playVoice")
    fun playVoice(
        view: View, url: String?
    ) {
        if (view is LinearLayout && url != null) {
            val button = view.findViewById<ImageButton>(R.id.play_pause_button)
            val seekBar = view.findViewById<SeekBar>(R.id.voice_seek_bar)
            val durationTextView = view.findViewById<TextView>(R.id.voice_duration)
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepare()
            val duration = mediaPlayer.duration
            val minutes = TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
            val seconds =
                TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) - TimeUnit.MINUTES.toSeconds(
                    minutes
                )
            val durationString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            durationTextView.text = durationString
            seekBar.max = duration
            val handler = Handler(Looper.myLooper()!!)
            var flags: Boolean = false
            handler.postDelayed(object : Runnable {
                override fun run() {
                    val currentPosition = mediaPlayer.currentPosition
                    if (!flags) seekBar.progress = (currentPosition)
                    handler.postDelayed(this, 100)
                }
            }, 100)


            mediaPlayer.setOnCompletionListener {
                flags = true
                seekBar.progress = 0
                animateImageChange(button, R.drawable.ic_baseline_play_arrow_24)
                button.tag = view.context.getString(R.string.play)
            }
            button.setOnClickListener {
                if (it.tag == view.context.getString(R.string.play)) {
                    animateImageChange(button, R.drawable.ic_baseline_pause_24)
                    it.tag = view.context.getString(R.string.pause)
                    mediaPlayer.start()
                    flags = false
                } else {
                    animateImageChange(button, R.drawable.ic_baseline_play_arrow_24)
                    it.tag = view.context.getString(R.string.play)
                    mediaPlayer.pause()
                }
            }
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress)
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
        } else imageView.visibility = View.INVISIBLE
    }
}