package com.m391.primavera

import android.content.Context
import androidx.lifecycle.asLiveData
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class DailyTaskWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    private val watchUId = DeviceIdGenerator.generateDeviceId()
    private val firestoreStepsHistory =
        FirebaseFirestore.getInstance().collection("HealthHistory").document(watchUId)
            .collection("Steps")
    private val firestore = FirebaseFirestore.getInstance().collection("Watches").document(watchUId)
    private lateinit var dataStoreManager: DataStoreManager
    override suspend fun doWork(): Result {
        dataStoreManager = DataStoreManager.getInstance(applicationContext)
        var totalSteps = dataStoreManager.stepsTodayFlow.asLiveData().value!!
        val daySteps = firestore.get().await()["Steps"] as Number
        totalSteps += daySteps.toFloat()
        dataStoreManager.setAllSteps(totalSteps)
        firestoreStepsHistory.add(
            hashMapOf(
                "value" to daySteps,
                "time" to Calendar.getInstance().time
            )
        )
        firestore.update(hashMapOf("Steps" to 0) as Map<String, Any>).await()
        return Result.success()
    }

    companion object {
        const val WORK_TAG = "DailyTaskWorker"
    }
}
