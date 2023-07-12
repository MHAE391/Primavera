package com.m391.primavera

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.SensorPrivacyManager.Sensors
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.m391.primavera.databinding.ActivitySensorsBinding

class SensorsActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private lateinit var binding: ActivitySensorsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        binding = ActivitySensorsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    @SuppressLint("LogNotTimber")
    override fun onStart() {
        super.onStart()
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor in sensors) {
            val sensorName = sensor.name
            val sensorType = sensor.type
            // Print or store the sensor information as per your requirements
            Log.d("SensorDetail", "Name: $sensorName, Type: $sensorType")
        }
    }

}