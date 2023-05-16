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
import com.m391.primavera.utils.models.ServerChildModel
import com.m391.primavera.utils.models.ServerFatherModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FatherHomeViewModel(app: Application) : BaseViewModel(app) {
    private val _fatherInformation = MutableLiveData<ServerFatherModel>()
    val fatherInformation: LiveData<ServerFatherModel> = _fatherInformation
    private val dataStore = DataStoreManager.getInstance(app.applicationContext)
    private val serverDatabase = ServerDatabase(app.applicationContext, dataStore)
    private val fathers = serverDatabase.fatherInformation
    private val children = serverDatabase.childInformation
    private val conversations = serverDatabase.conversations
    private val auth = serverDatabase.authentication

    private val _currentChildInformation = MutableLiveData<ServerChildModel>()
    val currentChildInformation: LiveData<ServerChildModel> = _currentChildInformation

    val currentChildUID = MutableLiveData<String>()
    private val currentFatherUID = MutableLiveData<String>()

    init {
        viewModelScope.launch {
            currentChildUID.postValue(dataStore.getCurrentChildUid()!!)
            currentFatherUID.postValue(auth.getCurrentUser()!!.uid)
        }
    }

    suspend fun openStreamChild(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        currentChildUID.observe(lifecycleOwner) {
            if (it != null) {
                viewModelScope.launch {
                    children.streamChildInformationByUID(it)
                        .observe(lifecycleOwner, Observer { serverChild ->
                            _currentChildInformation.postValue(serverChild)
                        })
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

    suspend fun closeStreamChild(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        children.streamChildInformationByUID(currentChildUID.value!!)
            .removeObservers(lifecycleOwner)
        currentChildUID.removeObservers(lifecycleOwner)
        withContext(Dispatchers.IO) {
            children.closeChildStream()
        }
    }

    suspend fun closeStreamFather(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        fathers.streamFatherInformationByUID(currentFatherUID.value!!)
            .removeObservers(lifecycleOwner)
        currentFatherUID.removeObservers(lifecycleOwner)
        withContext(Dispatchers.IO) {
            fathers.closeFatherStream()
        }
    }

    suspend fun createConversation() = withContext(Dispatchers.IO) {
        conversations.createConversation(currentChildUID.value!!)
    }
}