package com.m391.primavera

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.m391.primavera.databinding.ActivityPrimaveraBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class Primavera : AppCompatActivity() {
    private lateinit var binding: ActivityPrimaveraBinding
    private val dangerViewModel: DangerViewModel by viewModels()
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPrimaveraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataStoreManager = DataStoreManager.getInstance(context = applicationContext)
        dangerViewModel.setUpData()

    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()

        binding.chat.setOnClickListener {
            if (dangerViewModel.fatherUid.value != null) {
                startActivity(Intent(this, ChatActivity::class.java))
            } else Toast.makeText(this, "Setup Watch Data to Open Chat", Toast.LENGTH_SHORT).show()
        }
        binding.newChild.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        binding.danger.setOnClickListener {
            if (dangerViewModel.fatherUid.value != null) {
                fusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(this@Primavera)
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        lifecycleScope.launch {
                            dangerViewModel.dangerMode(this@Primavera, longitude, latitude)
                        }
                    }
                }
                Toast.makeText(this, "Danger Mode Activated", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Can't activate Danger Mode", Toast.LENGTH_SHORT).show()

        }

    }
}