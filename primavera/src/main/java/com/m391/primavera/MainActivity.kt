package com.m391.primavera

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
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
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.m391.primavera.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var id: String
    private val firestore = FirebaseFirestore.getInstance()

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        id = Settings.Secure.getString(
            applicationContext.contentResolver, Settings.Secure.ANDROID_ID
        )
        val qrCodeSize = 512 // Set the size of the QR code
        val hints = mapOf(
            Pair(EncodeHintType.CHARACTER_SET, "UTF-8"), // Set the character set
            Pair(EncodeHintType.MARGIN, 2) // Set the margin size
        )

        val bitMatrix =
            MultiFormatWriter().encode(id, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hints)
        val qrCodeBitmap = BarcodeEncoder().createBitmap(bitMatrix)
        binding.qrCode.setImageBitmap(qrCodeBitmap)
        notificationManager = NotificationManagerCompat.from(this)
    }

    private lateinit var notificationManager: NotificationManagerCompat

    override fun onStart() {
        super.onStart()
        val data = mapOf(
            "id" to id, "scanned" to "No"
        )
        //firestore.collection("Watches").document(id).set(data)
        firestore.collection("Watches").document(id).addSnapshotListener { value, _ ->
            if (value!!["scanned"] == "Yes") {

                startActivity(Intent(this@MainActivity, SecondActivity::class.java))
                finish()
            }
        }
    }


}