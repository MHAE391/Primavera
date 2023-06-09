package com.m391.primavera.user.father.search

import android.app.Application
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.NavigationCommand
import com.m391.primavera.utils.models.ServerTeacherModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FatherTeacherSearchViewModel(
    app: Application
) : BaseViewModel(app) {
    private val dataStoreManager: DataStoreManager =
        DataStoreManager.getInstance(app.applicationContext)
    private val serverDatabase: ServerDatabase =
        ServerDatabase(app.applicationContext, dataStoreManager)
    private val serverTeachers =
        serverDatabase.teacherInformation
    private val auth = serverDatabase.authentication
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
                    if (teacherMap[domainTeacher.teacherId] == null && domainTeacher.teacherId != auth.getCurrentUser()!!.uid) {
                        teachersArray.add(domainTeacher)
                        _teachersList.value = teachersArray
                        teacherMap[domainTeacher.teacherId] = domainTeacher
                    } else {
                        if (domainTeacher.teacherId != auth.getCurrentUser()!!.uid) {
                            val storedTeacher = teacherMap[domainTeacher.teacherId]
                            if (storedTeacher!!.firstName != domainTeacher.firstName ||
                                storedTeacher.lastName != domainTeacher.lastName ||
                                storedTeacher.dateOfBarth != domainTeacher.dateOfBarth ||
                                storedTeacher.academicYears != domainTeacher.academicYears ||
                                storedTeacher.subjects != domainTeacher.subjects ||
                                storedTeacher.longitude != domainTeacher.longitude ||
                                storedTeacher.latitude != domainTeacher.latitude ||
                                storedTeacher.imageUri != domainTeacher.imageUri ||
                                storedTeacher.phone != domainTeacher.phone ||
                                storedTeacher.rate != domainTeacher.rate
                            ) {
                                teachersArray.remove(storedTeacher)
                                teachersArray.add(domainTeacher)
                                teacherMap[domainTeacher.teacherId] = domainTeacher
                            }
                        }
                    }
                    _teachersList.value = teachersArray.sortedBy {
                        it.rate.toInt()
                    }.reversed()
                    validateData()
                }
            })
            validateData()
        }
    }

    suspend fun closeStream(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        serverTeachers.streamTeachers().removeObservers(lifecycleOwner)
        _teachersList.removeObservers(lifecycleOwner)
        withContext(Dispatchers.IO) {
            serverTeachers.closeUsersStream()
        }
    }

    fun backToConversation() {
        navigationCommand.value = NavigationCommand.Back
    }

    val searchWatcher = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(text: String?): Boolean {
            val searchTeacherArray = ArrayList<ServerTeacherModel>()
            teachersArray.forEach { storedTeacher ->
                if (text != null) {
                    if (storedTeacher.firstName.contains(text.toRegex())
                        || storedTeacher.lastName.contains(text.toRegex())
                    ) {
                        searchTeacherArray.add(storedTeacher)
                        _teachersList.value = searchTeacherArray.sortedBy {
                            it.rate.toInt()
                        }.reversed()
                        validateData()
                    }
                }
            }
            return true
        }

        override fun onQueryTextChange(text: String?): Boolean {
            val searchTeacherArray = ArrayList<ServerTeacherModel>()
            teachersArray.forEach { storedTeacher ->
                if (text != null) {
                    if (storedTeacher.firstName.lowercase(Locale.getDefault()).contains(
                            text.lowercase(Locale.getDefault()).toRegex()
                        )
                        || storedTeacher.lastName.lowercase(Locale.getDefault())
                            .contains(text.lowercase(Locale.getDefault()).toRegex())
                    ) {
                        searchTeacherArray.add(storedTeacher)
                        _teachersList.value = searchTeacherArray.sortedBy {
                            it.rate.toInt()
                        }.reversed()
                        validateData()
                    }
                }
            }
            _teachersList.value = searchTeacherArray.sortedBy {
                it.rate.toInt()
            }.reversed()
            validateData()

            return true
        }
    }

    fun validateData() {

        showNoData.value = _teachersList.value.isNullOrEmpty()
    }
}