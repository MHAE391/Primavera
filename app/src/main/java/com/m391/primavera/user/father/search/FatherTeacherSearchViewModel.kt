package com.m391.primavera.user.father.search

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.models.ServerTeacherModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FatherTeacherSearchViewModel(app: Application) : BaseViewModel(app) {
    private val dataStoreManager = DataStoreManager.getInstance(app.applicationContext)
    private val serverTeachers =
        ServerDatabase(app.applicationContext, dataStoreManager).teacherInformation
    private val _teachersList = MutableLiveData<List<ServerTeacherModel>>()
    val teachersList: LiveData<List<ServerTeacherModel>> = _teachersList
    private val teacherMap = HashMap<String, ServerTeacherModel>()
    private val teachersArray = ArrayList<ServerTeacherModel>()


    init {
        viewModelScope.launch {
            fetchOfflineData()
        }
    }

    private fun invalidateShowNoData() {
        showNoData.value = teachersList.value.isNullOrEmpty()
    }

    private suspend fun fetchOfflineData() {
        viewModelScope.launch {

        }

    }

    suspend fun openOnlineStream(lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            serverTeachers.streamTeachers().observe(lifecycleOwner, Observer { domainTeachers ->
                domainTeachers.forEach { domainTeacher ->
                    if (teacherMap[domainTeacher.teacherId] == null) {
                        teachersArray.add(domainTeacher)
                        _teachersList.value = teachersArray
                        teacherMap[domainTeacher.teacherId] = domainTeacher
                    } else {
                        val storedTeacher = teacherMap[domainTeacher.teacherId]
                        if (storedTeacher!!.firstName != domainTeacher.firstName ||
                            storedTeacher.lastName != domainTeacher.lastName ||
                            storedTeacher.age != domainTeacher.age ||
                            storedTeacher.academicYears != domainTeacher.academicYears ||
                            storedTeacher.subjects != domainTeacher.subjects ||
                            storedTeacher.longitude != domainTeacher.longitude ||
                            storedTeacher.latitude != domainTeacher.latitude ||
                            storedTeacher.imageUri != domainTeacher.imageUri ||
                            storedTeacher.phone != domainTeacher.phone
                        ) {
                            teachersArray.remove(storedTeacher)
                            teachersArray.add(domainTeacher)
                            _teachersList.value = teachersArray
                            teacherMap[domainTeacher.teacherId] = domainTeacher
                        }
                    }
                }
            })
            invalidateShowNoData()
        }
    }

    suspend fun closeStream(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        serverTeachers.streamTeachers().removeObservers(lifecycleOwner)
        withContext(Dispatchers.IO) {
            serverTeachers.closeUsersStream()
        }
    }
}