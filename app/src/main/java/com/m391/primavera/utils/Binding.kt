package com.m391.primavera.utils

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.firebase.storage.FirebaseStorage
import com.m391.primavera.R
import com.m391.primavera.user.father.teacher.TeacherProfileViewModel
import com.m391.primavera.utils.models.ServerTeacherModel
import com.squareup.picasso.Picasso

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
                val bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
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
}