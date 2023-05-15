package com.m391.primavera.user.father.teacher

import android.app.Application
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

class TeacherProfileViewModel(app: Application) : BaseViewModel(app) {
    private val _teacherUid = MutableLiveData<String>()
    val teacherUid: LiveData<String> = _teacherUid

    private val _teacherFirstName = MutableLiveData<String>()
    val teacherFirstName: LiveData<String> = _teacherFirstName

    private val _teacherLastName = MutableLiveData<String>()
    val teacherLastName: LiveData<String> = _teacherLastName

    private val _teacherImage = MutableLiveData<Any>()
    val teacherImage: LiveData<Any> = _teacherImage

    private val _teacherImageUri = MutableLiveData<String>()
    val teacherImageUri: LiveData<String> = _teacherImageUri

    private val _teacherLongitude = MutableLiveData<Number>()
    val teacherLongitude: LiveData<Number> = _teacherLongitude

    private val _teacherLatitude = MutableLiveData<Number>()
    val teacherLatitude: LiveData<Number> = _teacherLatitude

    private val _teacherRate = MutableLiveData<Number>()
    val teacherRate: LiveData<Number> = _teacherRate

    private val _teacherAge = MutableLiveData<String>()
    val teacherImageAge: LiveData<String> = _teacherAge

    private val _teacherPhone = MutableLiveData<String>()
    val teacherPhone: LiveData<String> = _teacherPhone

    private val _teacherAcademicYears = MutableLiveData<List<String>>()
    val teacherAcademicYears: LiveData<List<String>> = _teacherAcademicYears

    private val _teacherSubjects = MutableLiveData<List<String>>()
    val teacherSubjects: LiveData<List<String>> = _teacherSubjects


    private val dataStore = DataStoreManager.getInstance(app.applicationContext)
    private val teachers = ServerDatabase(app.applicationContext, dataStore).teacherInformation
    private val conversations = ServerDatabase(app.applicationContext, dataStore).conversations
    private val _teacherData = MutableLiveData<ServerTeacherModel>()
    val teacherData: LiveData<ServerTeacherModel> = _teacherData

    suspend fun openStream(lifecycleOwner: LifecycleOwner, uid: String) =
        withContext(Dispatchers.Main)
        {
            teachers.streamTeacherByUid(uid).observe(lifecycleOwner, Observer { serverTecher ->
                /* _teacherFirstName.value = it.firstName
                 _teacherLastName.value = it.lastName
                 _teacherAge.value = it.age
                 _teacherImage.value = it.image
                 _teacherImageUri.value = it.imageUri
                 _teacherAcademicYears.value = it.academicYears
                 _teacherSubjects.value = it.subjects
                 _teacherLongitude.value = it.longitude
                 _teacherLatitude.value = it.latitude
                 _teacherRate.value = it.rate*/
                _teacherData.value = serverTecher
            })
        }

    fun backToSearch() {
        navigationCommand.value = NavigationCommand.Back
    }

    fun closeStream(lifecycleOwner: LifecycleOwner, uid: String) = viewModelScope.launch {
        teachers.streamTeacherByUid(uid).removeObservers(lifecycleOwner)
        withContext(Dispatchers.IO) {
            teachers.closeUsersStream()
        }
    }

    suspend fun createConversation() = withContext(Dispatchers.IO) {
        conversations.createConversation(_teacherData.value!!.teacherId)
    }
}