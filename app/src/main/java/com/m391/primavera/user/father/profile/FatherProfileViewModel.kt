package com.m391.primavera.user.father.profile

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.Constants.NO_IMAGE
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.NavigationCommand
import com.m391.primavera.utils.models.ServerFatherModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.canParseAsIpAddress

class FatherProfileViewModel(
    app: Application
) : BaseViewModel(app) {
    private val dataStoreManager: DataStoreManager =
        DataStoreManager.getInstance(app.applicationContext)
    private val serverDatabase: ServerDatabase =
        ServerDatabase(app.applicationContext, dataStoreManager)
    private val fathers = serverDatabase.fatherInformation
    private val childrens = serverDatabase.childInformation
    private val auth = serverDatabase.authentication
    private val conversations = serverDatabase.conversations
    private val _fatherInfo = MutableLiveData<ServerFatherModel>()
    val fatherInfo: LiveData<ServerFatherModel> = _fatherInfo
    private val _children = MutableLiveData<String>()
    val children: LiveData<String> = _children

    private val _newImageUri = MutableLiveData<String>()
    val newImageUri: LiveData<String> = _newImageUri

    private val _newLongitude = MutableLiveData<Number>()
    val newLongitude: LiveData<Number> = _newLongitude

    private val _newLatitude = MutableLiveData<Number>()
    val newLatitude: LiveData<Number> = _newLatitude


    fun setNewImage(uri: String) {
        _newImageUri.postValue(uri)
    }

    private val childrenUID = ArrayList<String>()
    suspend fun openStream(lifecycleOwner: LifecycleOwner, uid: String) =
        withContext(Dispatchers.Main) {

            fathers.streamFatherInformationByUID(uid)
                .observe(lifecycleOwner) {
                    _fatherInfo.postValue(it)
                    var childs: String = ""
                    it.children.forEach { childUID ->
                        childrenUID.add(childUID)
                        viewModelScope.launch {
                            childrens.streamChildInformationByUID(childUID)
                                .observe(lifecycleOwner) { child ->
                                    childs += "- ${child.childName}\n"
                                    _children.postValue(childs.trim())
                                }
                        }
                    }
                }
        }

    fun checkChildFather(fatherUid: String): Boolean {
        return auth.getCurrentUser()!!.uid == fatherUid
    }

    suspend fun createConversation(uid: String) =
        withContext(Dispatchers.IO) {
            conversations.createTeacherWithFatherConversation(uid)
        }

    fun backToHome() {
        navigationCommand.value = NavigationCommand.Back
    }

    fun closeStream(lifecycleOwner: LifecycleOwner, uid: String) = viewModelScope.launch {
        fathers.streamFatherInformationByUID(uid)
            .removeObservers(lifecycleOwner)
        childrenUID.forEach {
            childrens.streamChildInformationByUID(it).removeObservers(lifecycleOwner)
        }
        withContext(Dispatchers.IO) {
            fathers.closeFatherStream()
            childrens.closeChildStream()
        }
    }

    fun setLocation(longitude: Double, latitude: Double) {
        _newLatitude.postValue(latitude)
        _newLongitude.postValue(longitude)
    }

    suspend fun updateFatherInfo() = viewModelScope.launch {
        showLoading.value = true
        if (newLongitude.value == null && newLatitude.value == null && newImageUri.value == null) {
            showToast.value = "Nothing To Change"
            showLoading.value = false
        } else {
            var response = SUCCESS
            if (newLongitude.value == null && newLatitude.value == null) {
                response = fathers.updateFatherInformation(
                    fatherInfo.value!!.longitude,
                    fatherInfo.value!!.latitude,
                    newImageUri.value!!
                )
            } else if (newLongitude.value != null && newLatitude.value != null && newImageUri.value == null) {
                response = fathers.updateFatherInformation(
                    newLongitude.value!!,
                    newLatitude.value!!,
                    NO_IMAGE
                )
            } else {
                response = fathers.updateFatherInformation(
                    newLongitude.value!!,
                    newLatitude.value!!,
                    newImageUri.value!!
                )
            }
            showLoading.value = false
            if (response == SUCCESS) {
                showToast.value = "Updated Successfully"
            } else {
                showToast.value = "Update Failed"
            }
        }
    }
}