package com.m391.primavera

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.lifecycle.lifecycleScope
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorPrivacyManager.Sensors
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.activityViewModels
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.*
import androidx.work.await
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.m391.primavera.DeviceIdGenerator.generateDeviceId
import com.m391.primavera.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var id: String
    private val firestore = FirebaseFirestore.getInstance()
    private var registration: ListenerRegistration? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
       // notificationPermission()
        id = generateDeviceId()

        val qrCodeSize = 512 // Set the size of the QR code
        val hints = mapOf(
            Pair(EncodeHintType.CHARACTER_SET, "UTF-8"), // Set the character set
            Pair(EncodeHintType.MARGIN, 2) // Set the margin size
        )

        val bitMatrix =
            MultiFormatWriter().encode(id, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hints)
        val qrCodeBitmap = BarcodeEncoder().createBitmap(bitMatrix)
        binding.qrCode.setImageBitmap(qrCodeBitmap)
        binding.qrCode.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }


    }

    private fun notificationPermission() {
        val builder = AlertDialog.Builder(this)
        val notificationManager = NotificationManagerCompat.from(this)
        if (!notificationManager.areNotificationsEnabled()) {
            builder.setTitle("Enable Notification")
                .setMessage("Notification is required for this app. Do you want to enable it?")
                .setPositiveButton("Yes") { _, _ ->
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    startActivity(intent)
                }.setNegativeButton("No") { dialog, _ ->
                    Toast.makeText(
                        this,
                        "You will not receive any notification",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.cancel()
                }.show()
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My Notification Channel"
            val descriptionText = "Channel Description"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("MHAE391", name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onPause() {
        super.onPause()
        if (registration != null) registration!!.remove()
    }
}