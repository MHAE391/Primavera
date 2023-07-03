package com.m391.primavera.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.m391.primavera.utils.models.LocalTeacherModel
import com.m391.primavera.utils.models.ServerTeacherModel
import java.util.*


fun List<ServerTeacherModel>.asLocalModel(): Array<LocalTeacherModel> {
    return map {
        LocalTeacherModel(
            teacherId = it.teacherId,
            firstName = it.firstName,
            lastName = it.lastName,
            phone = it.phone,
            image = it.image as ByteArray,
            imageUri = it.imageUri,
            longitude = it.longitude,
            latitude = it.latitude,
            academicYears = it.academicYears,
            subjects = it.subjects,
            age = it.dateOfBarth,
            rate = it.rate
        )
    }.toTypedArray()
}

fun List<LocalTeacherModel>.asServerModel(): List<ServerTeacherModel> {
    return map {
        ServerTeacherModel(
            teacherId = it.teacherId,
            firstName = it.firstName,
            lastName = it.lastName,
            phone = it.phone,
            image = it.image,
            imageUri = it.imageUri,
            longitude = it.longitude,
            latitude = it.latitude,
            academicYears = it.academicYears,
            subjects = it.subjects,
            dateOfBarth = it.age,
            rate = it.rate
        )
    }
}

fun <T> RecyclerView.setupGridRecycler(
    adapter: BaseRecyclerViewAdapter<T>
) {
    this.apply {
        layoutManager = GridLayoutManager(this.context, 2)
        this.adapter = adapter
    }
}

fun <T> RecyclerView.setupLinearRecycler(
    adapter: BaseRecyclerViewAdapter<T>
) {
    this.apply {
        layoutManager = LinearLayoutManager(this.context)
        this.adapter = adapter
    }
}

//animate changing the view visibility
fun View.fadeIn() {
    this.visibility = View.VISIBLE
    this.alpha = 0f
    this.animate().alpha(1f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            this@fadeIn.alpha = 1f
        }
    })
}

//animate changing the view visibility
fun View.fadeOut() {
    this.animate().alpha(0f).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            this@fadeOut.alpha = 1f
            this@fadeOut.visibility = View.GONE
        }
    })
}

fun checkForInternet(context: Context): Boolean {

    // register activity with the connectivity manager service
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // if the android version is equal to M
    // or greater we need to use the
    // NetworkCapabilities to check what type of
    // network has the internet connection
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

        // Returns a Network object corresponding to
        // the currently active default data network.
        val network = connectivityManager.activeNetwork ?: return false

        // Representation of the capabilities of an active network.
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            // Indicates this network uses a Wi-Fi transport,
            // or WiFi has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

            // Indicates this network uses a Cellular transport. or
            // Cellular has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

            // else return false
            else -> false
        }
    } else {
        // if the android version is below M
        @Suppress("DEPRECATION") val networkInfo =
            connectivityManager.activeNetworkInfo ?: return false
        @Suppress("DEPRECATION")
        return networkInfo.isConnected
    }
}

fun capitalize(value: String): String {
    return value.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}