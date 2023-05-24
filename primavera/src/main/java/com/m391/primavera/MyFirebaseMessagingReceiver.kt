package com.m391.primavera

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.m391.primavera.DeviceIdGenerator.generateDeviceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class MyFirebaseMessagingReceiver : BroadcastReceiver() {

    @SuppressLint("ObsoleteSdkInt", "HardwareIds", "MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.extras != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "MHAE391"
                val channelName = "My Notification Channel"
                val channelDescription = "Description of My Notification Channel"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(channelId, channelName, importance)
                channel.description = channelDescription
                val notificationManager =
                    context.getSystemService(NotificationManager::class.java)
                notificationManager?.createNotificationChannel(channel)
            }
            // Get the FCM message data
            val type = intent.extras!!.getString("type")
            if (type == "Assign") {
                val childName = intent.extras!!.getString("childName")
                val fatherName = intent.extras!!.getString("fatherName")
                val childUID = intent.extras!!.getString("childUID")
                val fatherUID = intent.extras!!.getString("fatherUID")
                val intentActivity = Intent(context, ChatActivity::class.java)
                intentActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intentActivity,
                    PendingIntent.FLAG_IMMUTABLE
                )
                // Create the notification
                val notificationBuilder =
                    NotificationCompat.Builder(context, "MHAE391").setSmallIcon(R.drawable.logo)
                        .setContentTitle("Congrats $fatherName")
                        .setContentText("Watch Assigned Successfully to $childName")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setSilent(false)
                        .setVibrate(longArrayOf(0, 1000))
                        .setAutoCancel(false)

                // Show the notification
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(
                    System.currentTimeMillis().toInt(), notificationBuilder.build()
                )
                val id = generateDeviceId()
                runBlocking(Dispatchers.IO) {
                    FirebaseFirestore.getInstance().collection("Watches").document(id)
                        .update(
                            mapOf(
                                "childName" to "$childName",
                                "childUID" to childUID,
                                "fatherUID" to fatherUID,
                                "fatherName" to fatherName
                            )
                        ).await()
                }
            } else {
                val intentActivity = Intent(context, ChatActivity::class.java)
                intentActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intentActivity,
                    PendingIntent.FLAG_IMMUTABLE
                )
                // Create the notification
                val childName = intent.extras!!.getString("childName")
                val notificationBuilder =
                    NotificationCompat.Builder(context, "MHAE391").setSmallIcon(R.drawable.logo)
                        .setContentTitle("Message")
                        .setContentText("$childName Your Father Sent You Voice Message")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setSilent(false)
                        .setVibrate(longArrayOf(0, 1000))
                        .setAutoCancel(true)

                // Show the notification
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(
                    System.currentTimeMillis().toInt(), notificationBuilder.build()
                )
            }
        }

    }
}