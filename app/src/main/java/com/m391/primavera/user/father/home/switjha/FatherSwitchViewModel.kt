package com.m391.primavera.user.father.home.switjha

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.Constants.TEACHER
import com.m391.primavera.utils.models.ServerChildModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FatherSwitchViewModel(
    app: Application
) : BaseViewModel(app) {
    private val dataStoreManager: DataStoreManager =
        DataStoreManager.getInstance(app.applicationContext)
    private val serverDatabase: ServerDatabase =
        ServerDatabase(app.applicationContext, dataStoreManager)
    private val fatherChildrenUIDS = MutableLiveData<List<String>>()
    private val _myChildren = MutableLiveData<List<ServerChildModel>>()
    val myChildren: LiveData<List<ServerChildModel>> = _myChildren
    private val myChildrenArray = ArrayList<ServerChildModel>()
    private val children = serverDatabase.childInformation
    private val teachers = serverDatabase.teacherInformation
    val currentChild = MutableLiveData<String>()
    private val auth = serverDatabase.authentication
    private val messageInformation = serverDatabase.messageInformation

    fun setupUIDS(uids: List<String>, current: String) {
        fatherChildrenUIDS.postValue(uids)
        currentChild.postValue(current)
    }

    suspend fun getChildren(lifecycleOwner: LifecycleOwner) {
        fatherChildrenUIDS.observe(lifecycleOwner) { it ->
            it?.forEach { uid ->
                viewModelScope.launch {
                    val serverChildModel = children.getChildInformationByUID(uid)
                    serverChildModel.currentChild = currentChild.value!!
                    myChildrenArray.add(serverChildModel)
                    myChildrenArray.sortedBy { child -> child.childName }
                    _myChildren.postValue(myChildrenArray)
                }
            }
            myChildrenArray.clear()
        }
    }

    suspend fun closeObservers(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        fatherChildrenUIDS.removeObservers(lifecycleOwner)
    }


    suspend fun switchFatherTeacher(): Boolean = withContext(Dispatchers.IO) {
        return@withContext teachers.checkAlreadyTeacherOrNot()
    }

    suspend fun setCurrentUserType() = withContext(Dispatchers.IO) {
        dataStoreManager.setUserType(TEACHER)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        dataStoreManager.setUserType(null)
        dataStoreManager.setUserUid(null)
        dataStoreManager.setCurrentChildUid(null)
        messageInformation.unSubscribeToTopic()
        auth.logOut()
    }
}