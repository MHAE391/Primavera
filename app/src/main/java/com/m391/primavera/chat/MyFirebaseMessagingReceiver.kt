package com.m391.primavera.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.KeyguardManager
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
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.m391.primavera.BuildConfig
import com.m391.primavera.R
import com.m391.primavera.chat.child.alarm.AlarmActivity
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.Constants.MESSAGE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class MyFirebaseMessagingReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"
    }

    @SuppressLint("MissingPermission", "NewApi")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.extras!!.getString("type") == MESSAGE) {
            val receiverUid = "${intent.extras!!.getString("senderId")}"
            val notificationManager = NotificationManagerCompat.from(context.applicationContext)


            if (!isPhoneClosed(context)) {

                if (!isAppRunning(context)) {
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
                    var user: String
                    runBlocking {
                        val dataStoreManager = DataStoreManager.getInstance(context)
                        user = dataStoreManager.getCurrentChatReceiver()!!
                    }
                    if (user != "${intent.extras!!.getString("senderId")}") {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val name = context.getString(R.string.app_name)
                            val channel = NotificationChannel(
                                NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH
                            )
                            channel.enableVibration(false)
                            channel.setSound(
                                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                                null
                            )
                            channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                            notificationManager.createNotificationChannel(channel)
                        }
                        val notification =
                            NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                                .setSmallIcon(R.mipmap.logo)
                                .setContentTitle("${intent.extras!!.getString("senderName")}")
                                .setContentText("${intent.extras!!.getString("messageBody")}")
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setVibrate(longArrayOf(0))
                                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                .setDefaults(NotificationCompat.DEFAULT_ALL)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).build()
                        notificationManager.notify(
                            getNotificationId("${intent.extras!!.getString("senderId")}"),
                            notification
                        )
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val name = context.getString(R.string.app_name)
                    val channel = NotificationChannel(
                        NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH
                    )
                    channel.enableVibration(true)
                    channel.vibrationPattern = longArrayOf(1000)
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
                    .setVibrate(longArrayOf(1000))
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).build()
                notificationManager.notify(
                    getNotificationId("${intent.extras!!.getString("senderId")}"), notification
                )
            }
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

    private fun isPhoneClosed(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        // Check if the screen is off (phone closed)
        val isScreenOff = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            !powerManager.isInteractive
        } else {
            !powerManager.isScreenOn
        }

        // Check if the device is locked (keyguard active)
        val isKeyguardLocked = keyguardManager.isKeyguardLocked

        return isScreenOff && isKeyguardLocked
    }

    private fun isAppRunning(
        context: Context,
        packageName: String = "com.m391.primavera"
    ): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses

        for (processInfo in runningAppProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                processInfo.processName == packageName
            ) {
                return true
            }
        }
        return false
    }

    private fun getCurrentActivity(context: Context): String {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTaskInfoList = activityManager.getRunningTasks(1)
        val runningActivity = runningTaskInfoList[0].topActivity
        return runningActivity?.className!!
    }

}