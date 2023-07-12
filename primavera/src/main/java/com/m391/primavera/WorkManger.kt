package com.m391.primavera

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class WorkManger(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    private val watchUId = DeviceIdGenerator.generateDeviceId()
    private val firestoreStepsHistory =
        FirebaseFirestore.getInstance().collection("HealthHistory").document(watchUId)
            .collection("Steps")
    private val firestore = FirebaseFirestore.getInstance().collection("Watches").document(watchUId)

    override suspend fun doWork(): Result {
        val daySteps = firestore.get().await()["Steps"] as Number
        firestoreStepsHistory.add(
            hashMapOf(
                "value" to daySteps,
                "time" to Calendar.getInstance().time
            )
        )
        firestore.update(hashMapOf("Steps" to 0) as Map<String, Any>).await()
        return Result.success()
    }
}
