package com.m391.primavera

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import com.m391.primavera.DeviceIdGenerator.generateDeviceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {}


    override fun onNewToken(token: String) {
        val id = generateDeviceId()
        runBlocking(Dispatchers.IO) {
            if (!checkWatch(id)) {
                FirebaseFirestore.getInstance().collection("Watches").document(id)
                    .set(mapOf("token" to token, "watchUid" to id)).await()
            } else {
                FirebaseFirestore.getInstance().collection("Watches").document(id)
                    .update("token", token).await()
            }
        }
    }

    private suspend fun checkWatch(id: String): Boolean {
        val response =
            FirebaseFirestore.getInstance().collection("Watches").document(id).get().await()
        return response.exists()
    }
}