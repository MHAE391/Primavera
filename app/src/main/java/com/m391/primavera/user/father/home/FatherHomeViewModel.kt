package com.m391.primavera.user.father.home

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.models.LocationModel
import com.m391.primavera.utils.models.ServerChildModel
import com.m391.primavera.utils.models.ServerFatherModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import kotlin.math.*

class FatherHomeViewModel(
    app: Application
) : BaseViewModel(app) {
    private val dataStoreManager: DataStoreManager =
        DataStoreManager.getInstance(app.applicationContext)
    private val serverDatabase: ServerDatabase =
        ServerDatabase(app.applicationContext, dataStoreManager)
    private val _fatherInformation = MutableLiveData<ServerFatherModel>()
    val fatherInformation: LiveData<ServerFatherModel> = _fatherInformation
    private val fathers = serverDatabase.fatherInformation
    private val children = serverDatabase.childInformation
    private val conversations = serverDatabase.conversations
    private val auth = serverDatabase.authentication

    private val watches = serverDatabase.watches
    private val _currentChildInformation = MutableLiveData<ServerChildModel>()
    val currentChildInformation: LiveData<ServerChildModel> = _currentChildInformation

    val currentChildUID = MutableLiveData<String>()
    private val currentFatherUID = MutableLiveData<String>()

    val heartRate = MutableLiveData<String>()
    val oxygenLevel = MutableLiveData<String>()
    val steps = MutableLiveData<String>()

    init {
        viewModelScope.launch {
            setupCurrentChildAndFather()
        }
    }


    suspend fun setupCurrentChildAndFather() {
        viewModelScope.launch {
            currentChildUID.postValue(dataStoreManager.getCurrentChildUid()!!)
            currentFatherUID.postValue(auth.getCurrentUser()!!.uid)
        }
    }

    val watchLocationString = MutableLiveData<String>()
    val watchLocation = MutableLiveData<LocationModel>()
    suspend fun openStreamChild(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        currentChildUID.observe(lifecycleOwner) {
            if (it != null) {
                viewModelScope.launch {
                    children.streamChildInformationByUID(it)
                        .observe(lifecycleOwner) { serverChild ->
                            _currentChildInformation.postValue(serverChild)
                            if (serverChild != null) {
                                viewModelScope.launch {
                                    watches.openWatchStream(serverChild.watchUID)
                                        .observe(lifecycleOwner) { status ->
                                            if (status != null && fatherInformation.value != null) {
                                                val distance = calculateDistance(
                                                    fatherInformation.value!!.latitude.toDouble(),
                                                    fatherInformation.value!!.longitude.toDouble(),
                                                    status.latitude,
                                                    status.longitude
                                                )
                                                if (distance.toDouble() < 1.0) {
                                                    watchLocationString.postValue(
                                                        "${distance.toDouble() * 1000} M From Home"
                                                    )
                                                } else {
                                                    watchLocationString.postValue(
                                                        "$distance KM From Home"
                                                    )
                                                }
                                                watchLocation.value = LocationModel(
                                                    longitude = status.longitude,
                                                    latitude = status.latitude
                                                )
                                                steps.postValue(
                                                    status.dailySteps.toInt().toString()
                                                )
                                                heartRate.postValue(
                                                    status.heartRate.toInt().toString()
                                                )
                                                oxygenLevel.postValue(
                                                    status.oxygenLevel.toInt().toString()
                                                )
                                            }
                                        }
                                }
                            }
                        }
                }
            }
        }
    }

    suspend fun openStreamFather(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        currentFatherUID.observe(lifecycleOwner) {
            if (it != null) {
                viewModelScope.launch {
                    fathers.streamFatherInformationByUID(auth.getCurrentUser()?.uid!!)
                        .observe(lifecycleOwner, Observer { serverFather ->
                            _fatherInformation.postValue(serverFather)
                        })
                }
            }
        }

    }

    suspend fun closeWatchSteam(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        if (currentChildInformation.value != null) {
            watches.openWatchStream(currentChildInformation.value!!.watchUID)
                .removeObservers(lifecycleOwner)
            withContext(Dispatchers.IO) {
                watches.closeWatchStream()
            }
        }
    }

    suspend fun closeStreamChild(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        if (currentChildUID.value != null) {
            children.streamChildInformationByUID(currentChildUID.value!!)
                .removeObservers(lifecycleOwner)
            currentChildUID.removeObservers(lifecycleOwner)
            withContext(Dispatchers.IO) {
                children.closeChildStream()
            }
        }
    }

    suspend fun closeStreamFather(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        if (currentFatherUID.value != null) {
            fathers.streamFatherInformationByUID(currentFatherUID.value!!)
                .removeObservers(lifecycleOwner)
            currentFatherUID.removeObservers(lifecycleOwner)
            withContext(Dispatchers.IO) {
                fathers.closeFatherStream()
            }
        }
    }

    suspend fun createConversation() = withContext(Dispatchers.IO) {
        conversations.createFatherWithTeacherConversation(currentChildUID.value!!)
    }

    suspend fun changeCurrentChild(childUID: String) = viewModelScope.launch {
        dataStoreManager.setCurrentChildUid(childUID)
        currentChildUID.postValue(childUID)
    }

    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): String {
        val earthRadius = 6371.0 // Earth's radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return formatDouble(earthRadius * c)
    }

    private fun formatDouble(number: Double): String {
        val decimalFormat = DecimalFormat("0.00")
        return decimalFormat.format(number)
    }
}