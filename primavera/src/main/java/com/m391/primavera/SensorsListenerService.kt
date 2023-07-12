package com.m391.primavera

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.m391.primavera.DeviceIdGenerator.generateDeviceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class SensorsListenerService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private val watchUId = generateDeviceId()
    private val firestore = FirebaseFirestore.getInstance().collection("Watches").document(watchUId)
    private val firestoreHeartRateHistory =
        FirebaseFirestore.getInstance().collection("HealthHistory").document(watchUId)
            .collection("HeartRate")

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor in sensors) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used in this example
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            when (event.sensor.type) {
                Sensor.TYPE_HEART_RATE -> {
                    if (event.values[0].toInt() != 0) {
                        firestore.update(
                            hashMapOf(
                                "HeartRate" to event.values[0]
                            ) as Map<String, Any>
                        )
                        firestoreHeartRateHistory.add(
                            hashMapOf(
                                "value" to event.values[0],
                                "time" to Calendar.getInstance().time
                            )
                        )

                    } else firestore.update(hashMapOf("isWorn" to "No") as Map<String, Any>)
                }

                Sensor.TYPE_STEP_COUNTER -> {
                    firestore.update(hashMapOf("Steps" to event.values[0]) as Map<String, Any>)
                }

                Sensor.TYPE_STEP_DETECTOR -> {
                    firestore.update(hashMapOf("StepsDetector" to event.values[0]) as Map<String, Any>)
                }
            }
        }
    }

}