package com.m391.primavera.chat

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.m391.primavera.BuildConfig
import com.m391.primavera.R
import com.m391.primavera.chat.child.alarm.AlarmActivity
import com.m391.primavera.utils.Constants.MESSAGE


class MyFirebaseMessagingReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"
    }

    @SuppressLint("MissingPermission", "NewApi")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.extras!!.getString("type") == MESSAGE) {
            val notificationManager = NotificationManagerCompat.from(context.applicationContext)
            val vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context.getString(R.string.app_name)
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH
                )
                channel.enableVibration(true)
                channel.setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null
                )
                channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                notificationManager.createNotificationChannel(channel)
            }
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.logo)
                .setContentTitle("${intent.extras!!.getString("senderName")}")
                .setContentText("${intent.extras!!.getString("messageBody")}")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).build()
            notificationManager.notify(
                getNotificationId("${intent.extras!!.getString("senderId")}"), notification
            )
        } else {
            val alarmIntent = Intent(context, AlarmActivity::class.java)
            alarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(alarmIntent)
        }
    }

    private fun getNotificationId(id: String): Int {
        var code: Int = 0
        id.forEach {
            code += it.code
        }
        return code
    }
}