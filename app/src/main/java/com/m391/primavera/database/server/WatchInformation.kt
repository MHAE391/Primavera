package com.m391.primavera.database.server

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.m391.primavera.utils.Constants.WATCHES
import com.m391.primavera.utils.models.LocationModel
import com.m391.primavera.utils.models.WatchStatusServerModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class WatchInformation(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val watches: CollectionReference = firestore.collection(WATCHES)
    private var registration: ListenerRegistration? = null

    suspend fun getWatchToken(id: String): String {
        val response = watches.document(id).get().await()
        return response.getString("token")!!
    }
    suspend fun openWatchStream(watchUid: String): LiveData<WatchStatusServerModel> =
        withContext(Dispatchers.IO) {
            val watchStatus = MutableLiveData<WatchStatusServerModel>()
            registration = watches.document(watchUid).addSnapshotListener { value, error ->
                if (error != null) {
                    Timber.tag("Watches Database").e(error, "Listen failed.")
                    return@addSnapshotListener
                }
                val status = WatchStatusServerModel(
                    oxygenLevel = value!!.data!!["OxygenLevel"] as Number,
                    heartRate = value.data!!["HeartRate"] as Number,
                    dailySteps = value.data!!["Steps"] as Number,
                    longitude = value.data!!["Longitude"]!! as Double,
                    latitude = value.data!!["Latitude"]!! as Double
                )
                watchStatus.postValue(status)
            }
            return@withContext watchStatus
        }


    suspend fun closeWatchStream() = withContext(Dispatchers.IO) {
        if (registration != null) {
            registration!!.remove()
        }
    }
}