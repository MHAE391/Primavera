package com.m391.primavera.user.father.home.child

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.models.HealthHistoryModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HealthHistoryViewModel(app: Application) : BaseViewModel(app) {
    private val _heartRateHistory = MutableLiveData<List<HealthHistoryModel>>()
    val heartRateHistory: LiveData<List<HealthHistoryModel>> = _heartRateHistory

    private val _oxygenLevelHistory = MutableLiveData<List<HealthHistoryModel>>()
    val oxygenLevelHistory: LiveData<List<HealthHistoryModel>> = _oxygenLevelHistory

    private val _stepsHistory = MutableLiveData<List<HealthHistoryModel>>()
    val stepsHistory: LiveData<List<HealthHistoryModel>> = _stepsHistory

    private val dataStoreManager = DataStoreManager.getInstance(app.applicationContext)
    private val serverDatabase = ServerDatabase(app.applicationContext, dataStoreManager)
    private val healthHistory = serverDatabase.healthHistoryInformation
    suspend fun openHeartRateHistoryStream(watchUid: String, lifecycleOwner: LifecycleOwner) =
        viewModelScope.launch {
            healthHistory.streamHeartRateHistory(watchUid).observe(lifecycleOwner) { historyList ->
                _heartRateHistory.postValue(historyList.sortedBy { it.time }.reversed())
                if (historyList.isNullOrEmpty()) showNoData.postValue(true)
                else showNoData.postValue(false)
            }
        }

    suspend fun openOxygenLevelHistoryStream(watchUid: String, lifecycleOwner: LifecycleOwner) =
        viewModelScope.launch {
            healthHistory.streamOxygenHistory(watchUid).observe(lifecycleOwner) { historyList ->
                _oxygenLevelHistory.postValue(historyList.sortedBy { it.time }.reversed())
                if (historyList.isNullOrEmpty()) showNoData.postValue(true)
                else showNoData.postValue(false)
            }
        }

    suspend fun openStepsHistoryStream(watchUid: String, lifecycleOwner: LifecycleOwner) =
        viewModelScope.launch {
            healthHistory.streamStepsHistory(watchUid).observe(lifecycleOwner) { historyList ->
                _stepsHistory.postValue(historyList.sortedBy { it.time }.reversed())
                if (historyList.isNullOrEmpty()) showNoData.postValue(true)
                else showNoData.postValue(false)
            }
        }

    suspend fun closeHeartRateHistoryStream(watchUid: String, lifecycleOwner: LifecycleOwner) {
        healthHistory.streamHeartRateHistory(watchUid).removeObservers(lifecycleOwner)
        withContext(Dispatchers.IO) {
            healthHistory.closeHealthStream()
        }
    }

    suspend fun closeOxygenLevelHistoryStream(watchUid: String, lifecycleOwner: LifecycleOwner) {
        healthHistory.streamOxygenHistory(watchUid).removeObservers(lifecycleOwner)
        withContext(Dispatchers.IO) {
            healthHistory.closeHealthStream()
        }
    }

    suspend fun closeStepsHistoryStream(watchUid: String, lifecycleOwner: LifecycleOwner) {
        healthHistory.streamStepsHistory(watchUid).removeObservers(lifecycleOwner)
        withContext(Dispatchers.IO) {
            healthHistory.closeHealthStream()
        }
    }

}