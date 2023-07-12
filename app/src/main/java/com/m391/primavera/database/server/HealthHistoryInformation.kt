package com.m391.primavera.database.server

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.utils.Constants.HEALTH_HISTORY
import com.m391.primavera.utils.Constants.HEART_RATE
import com.m391.primavera.utils.Constants.OXYGEN_LEVEL
import com.m391.primavera.utils.Constants.STEPS
import com.m391.primavera.utils.models.HealthHistoryModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date

class HealthHistoryInformation(
    private val context: Context,
    private val dataStoreManager: DataStoreManager
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val healthHistory: CollectionReference = firestore.collection(HEALTH_HISTORY)
    private var registration: ListenerRegistration? = null

    suspend fun streamHeartRateHistory(watchUid: String): LiveData<List<HealthHistoryModel>> =
        withContext(Dispatchers.IO) {
            val heartRateHistory = MutableLiveData<List<HealthHistoryModel>>()
            registration = healthHistory.document(watchUid).collection(HEART_RATE)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Timber.tag("Messages Database").e(error, "Listen failed.")
                        return@addSnapshotListener
                    }
                    val heartRateList = ArrayList<HealthHistoryModel>()
                    for (data in value!!) {
                        heartRateList.add(
                            HealthHistoryModel(
                                value = data["value"]!! as Number,
                                time = data["time", Date::class.java]!! as Date,
                                HEART_RATE
                            )
                        )
                    }
                    heartRateHistory.postValue(heartRateList)
                }
            return@withContext heartRateHistory
        }

    suspend fun streamOxygenHistory(watchUid: String): LiveData<List<HealthHistoryModel>> =
        withContext(Dispatchers.IO) {
            val oxygenLevelHistory = MutableLiveData<List<HealthHistoryModel>>()
            registration = healthHistory.document(watchUid).collection(OXYGEN_LEVEL)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Timber.tag("Messages Database").e(error, "Listen failed.")
                        return@addSnapshotListener
                    }
                    val oxygenLevelList = ArrayList<HealthHistoryModel>()
                    for (data in value!!) {
                        oxygenLevelList.add(
                            HealthHistoryModel(
                                value = data["value"]!! as Number,
                                time = data["time", Date::class.java]!! as Date,
                                OXYGEN_LEVEL
                            )
                        )
                    }
                    oxygenLevelHistory.postValue(oxygenLevelList)
                }
            return@withContext oxygenLevelHistory
        }

    suspend fun streamStepsHistory(watchUid: String): LiveData<List<HealthHistoryModel>> =
        withContext(Dispatchers.IO) {
            val stepsHistory = MutableLiveData<List<HealthHistoryModel>>()
            registration = healthHistory.document(watchUid).collection(STEPS)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Timber.tag("Messages Database").e(error, "Listen failed.")
                        return@addSnapshotListener
                    }
                    val stepsList = ArrayList<HealthHistoryModel>()
                    for (data in value!!) {
                        stepsList.add(
                            HealthHistoryModel(
                                value = data["value"]!! as Number,
                                time = data["time", Date::class.java]!! as Date,
                                STEPS
                            )
                        )
                    }
                    stepsHistory.postValue(stepsList)
                }
            return@withContext stepsHistory
        }

    suspend fun closeHealthStream() = withContext(Dispatchers.IO) {
        if (registration != null) {
            registration!!.remove()
        }
    }
}