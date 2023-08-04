package com.m391.primavera.chat.child.alarm

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.os.Bundle
import com.m391.primavera.R
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.m391.primavera.authentication.information.father.location.FatherLocationFragment
import com.m391.primavera.chat.ChatActivity
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.databinding.ActivityAlarmBinding
import com.m391.primavera.user.father.FatherActivity
import com.m391.primavera.user.teacher.TeacherActivity
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.properties.Delegates

class AlarmActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityAlarmBinding
    private lateinit var map: GoogleMap
    private lateinit var dataStore: DataStoreManager
    private var latitude by Delegates.notNull<Double>()
    private var longitude by Delegates.notNull<Double>()
    private lateinit var childName: String
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var childUid: String

    private lateinit var vibrator: Vibrator
    private val pattern = longArrayOf(0, 1000, 1000)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            setVisible(true)
            val keyManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyManager.requestDismissKeyguard(this, null)
        } else {
            window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            }
        }
        dataStore = DataStoreManager.getInstance(applicationContext)
        childName = intent.extras!!.getString("childName").toString()
        latitude = intent.extras!!.getString("latitude").toString().toDouble()
        longitude = intent.extras!!.getString("longitude").toString().toDouble()
        childUid = intent.extras!!.getString("childUid").toString()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        mediaPlayer = MediaPlayer.create(this, R.raw.worning_alarm)
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        vibrator.cancel()
        mediaPlayer.release()
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        binding.name.text = "Your Child $childName\nNeeds Help"
        binding.close.setOnClickListener {
            finish()
        }


        binding.openApp.setOnClickListener {
            lifecycleScope.launch {
                dataStore.setCurrentChildUid(childUid)
            }
            if (intent.extras!!.getString("running").toString() == "No") {
                startActivity(Intent(this, FatherActivity::class.java))
            } else {
                val intent = Intent(this, FatherActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            finish()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(pattern, 0) // Loop indefinitely
            vibrator.vibrate(vibrationEffect)
        } else {
            vibrator.vibrate(pattern, 0)
        }
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapStyle(map)
        displayLocationOnMap(
            latitude,
            longitude
        )
    }

    private fun displayLocationOnMap(latitude: Double, longitude: Double) {
        val location = LatLng(latitude, longitude)

        val cameraPosition = CameraPosition.Builder()
            .target(location)
            .zoom(30F)
            .build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        val poiMarker = map.addMarker(
            MarkerOptions().position(location)
                .title(
                    "$childName  Location"
                )
        )
        poiMarker?.showInfoWindow()
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.map_style
                )
            )
            if (!success) {
                Timber.tag("ads").e("Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Timber.tag("ads").e(e, "Can't find style. Error: ")
        }
    }

}
