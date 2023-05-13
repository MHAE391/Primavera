package com.m391.primavera.user.father.search

import android.app.Application
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.m391.primavera.R
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
            teachersArray.add(
                ServerTeacherModel(
                    "123",
                    "Ali",
                    "Salah",
                    "01025377909",
                    "1683729384732d7ea0bcc-6c40-4283-b79e-4bab8aebb012",
                    "https://firebasestorage.googleapis.com/v0/b/primavera-942dc.appspot.com/o/Images%2F1683729384732d7ea0bcc-6c40-4283-b79e-4bab8aebb012?alt=media&token=d6457ff1-1219-4ae2-bd3c-d255a6bede63",
                    11155,
                    111,
                    "27",
                    arrayListOf<String>(
                        app.getString(R.string.third_secondary),
                        app.getString(R.string.second_secondary),
                        app.getString(R.string.first_secondary)
                    ),
                    arrayListOf<String>("English"),
                    5
                )
            )
            teachersArray.add(
                ServerTeacherModel(
                    "12ss3",
                    "Morad",
                    "Salah",
                    "01013001808",
                    "1683729384732d7ea0bcc-6c40-4283-b79e-4bab8aebb012",
                    "https://firebasestorage.googleapis.com/v0/b/primavera-942dc.appspot.com/o/Images%2F1683729384732d7ea0bcc-6c40-4283-b79e-4bab8aebb012?alt=media&token=d6457ff1-1219-4ae2-bd3c-d255a6bede63",
                    1151,
                    111,
                    "25",
                    arrayListOf<String>(
                        app.getString(R.string.third_secondary),
                        app.getString(R.string.second_secondary),
                        app.getString(R.string.first_secondary)
                    ),
                    arrayListOf<String>("Arabic"),
                    1
                )
            )

            teachersArray.add(
                ServerTeacherModel(
                    "123ss",
                    "Amir",
                    "Salah",
                    "01004826321",
                    "1683729384732d7ea0bcc-6c40-4283-b79e-4bab8aebb012",
                    "https://firebasestorage.googleapis.com/v0/b/primavera-942dc.appspot.com/o/Images%2F1683729384732d7ea0bcc-6c40-4283-b79e-4bab8aebb012?alt=media&token=d6457ff1-1219-4ae2-bd3c-d255a6bede63",
                    1511,
                    111,
                    "20",
                    arrayListOf<String>(
                        app.getString(R.string.third_secondary),
                        app.getString(R.string.second_secondary),
                        app.getString(R.string.first_secondary)
                    ),
                    arrayListOf<String>("Math"),
                    2
                )
            )

            teachersArray.add(
                ServerTeacherModel(
                    "123",
                    "Alaa",
                    "Salah",
                    "01018969137",
                    "1683729384732d7ea0bcc-6c40-4283-b79e-4bab8aebb012",
                    "https://firebasestorage.googleapis.com/v0/b/primavera-942dc.appspot.com/o/Images%2F1683729384732d7ea0bcc-6c40-4283-b79e-4bab8aebb012?alt=media&token=d6457ff1-1219-4ae2-bd3c-d255a6bede63",
                    1511,
                    111,
                    "28",
                    arrayListOf<String>(
                        app.getString(R.string.third_secondary),
                        app.getString(R.string.second_secondary),
                        app.getString(R.string.first_secondary)
                    ),
                    arrayListOf<String>("french"),
                    4
                )
            )

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
                            storedTeacher.phone != domainTeacher.phone ||
                            storedTeacher.rate != domainTeacher.rate
                        ) {
                            teachersArray.remove(storedTeacher)
                            teachersArray.add(domainTeacher)
                            teacherMap[domainTeacher.teacherId] = domainTeacher
                        }
                    }
                    _teachersList.value = teachersArray.sortedBy {
                        it.rate.toInt()
                    }.reversed()
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
                    }
                }
            }
            _teachersList.value = searchTeacherArray.sortedBy {
                it.rate.toInt()
            }.reversed()

            return true
        }
    }
}