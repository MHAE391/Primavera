package com.m391.primavera.database.server

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.m391.primavera.utils.Constants.WATCHES
import com.m391.primavera.utils.models.LocationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class WatchInformation(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val watches: CollectionReference = firestore.collection(WATCHES)
    suspend fun getWatchToken(id: String): String {
        val response = watches.document(id).get().await()
        return response.getString("token")!!
    }

    fun getHeartRate(id: String): LiveData<String> {
        val heartRate = MutableLiveData<String>()
        val response = watches.document(id).addSnapshotListener { value, error ->
            if (error != null) {
                Timber.tag("Teachers Database").e(error, "Listen failed.")
                return@addSnapshotListener
            }
            heartRate.postValue(value!!.data?.get("HeartRate")!!.toString())
        }
        return heartRate
    }

    suspend fun getWatchLocation(id: String): LiveData<LocationModel> =
        withContext(Dispatchers.IO) {
            val location = MutableLiveData<LocationModel>()
            watches.document(id).addSnapshotListener { value, error ->
                if (error != null) {
                    Timber.tag("Teachers Database").e(error, "Listen failed.")
                    return@addSnapshotListener
                }
                location.postValue(
                    LocationModel(
                        longitude = value!!.data!!["Longitude"]!! as Double,
                        latitude = value.data!!["Latitude"]!! as Double
                    )
                )
            }
            return@withContext location
        }

    fun getStepsCount(id: String): LiveData<String> {
        val steps = MutableLiveData<String>()
        val response = watches.document(id).addSnapshotListener { value, error ->
            if (error != null) {
                Timber.tag("Teachers Database").e(error, "Listen failed.")
                return@addSnapshotListener
            }
            steps.postValue(value!!.data?.get("Steps")!!.toString())
        }
        return steps
    }

    fun getOxygenLevel(id: String): LiveData<String> {
        val oxygen = MutableLiveData<String>()
        val response = watches.document(id).addSnapshotListener { value, error ->
            if (error != null) {
                Timber.tag("Teachers Database").e(error, "Listen failed.")
                return@addSnapshotListener
            }
            oxygen.postValue(value!!.data?.get("OxygenLevel")!!.toString())
        }
        return oxygen
    }
}