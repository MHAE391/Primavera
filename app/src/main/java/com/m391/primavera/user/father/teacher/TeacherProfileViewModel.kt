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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    val teacherAge: LiveData<String> = _teacherAge

    private val _teacherPhone = MutableLiveData<String>()
    val teacherPhone: LiveData<String> = _teacherPhone

    private val _teacherAcademicYears = MutableLiveData<String>()
    val teacherAcademicYears: LiveData<String> = _teacherAcademicYears

    private val _teacherSubjects = MutableLiveData<String>()
    val teacherSubjects: LiveData<String> = _teacherSubjects


    private val dataStore = DataStoreManager.getInstance(app.applicationContext)
    private val teachers = ServerDatabase(app.applicationContext, dataStore).teacherInformation
    private val conversations = ServerDatabase(app.applicationContext, dataStore).conversations
    private val _teacherData = MutableLiveData<ServerTeacherModel>()
    val teacherData: LiveData<ServerTeacherModel> = _teacherData

    suspend fun openStream(lifecycleOwner: LifecycleOwner, uid: String) =
        withContext(Dispatchers.Main)
        {
            teachers.streamTeacherByUid(uid).observe(lifecycleOwner, Observer { serverTeacher ->
                /* _teacherFirstName.value = it.firstName
                 _teacherLastName.value = it.lastName
                 _teacherImage.value = it.image
                 _teacherImageUri.value = it.imageUri
                 _teacherAcademicYears.value = it.academicYears
                 _teacherSubjects.value = it.subjects
                 _teacherLongitude.value = it.longitude
                 _teacherLatitude.value = it.latitude
                 _teacherRate.value = it.rate*/
                var subjects: String = ""
                var years: String = ""
                serverTeacher.academicYears.forEach {
                    years += "- $it\n"
                    _teacherAcademicYears.value = years.trim()
                }
                serverTeacher.subjects.forEach {
                    subjects += "- $it\n"
                    _teacherSubjects.value = subjects.trim()
                }
                _teacherLongitude.value = serverTeacher.longitude
                _teacherLatitude.value = serverTeacher.latitude
                _teacherAge.value = calculateAge(serverTeacher.dateOfBarth).toString()
                _teacherData.value = serverTeacher
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

    private fun calculateAge(selectedDate: String): Int {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Calendar.getInstance()

        val birthDate = dateFormat.parse(selectedDate)
        val birthCalendar = Calendar.getInstance()
        birthCalendar.time = birthDate!!

        var age = currentDate.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)

        if (currentDate.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    suspend fun rateTeacher(rate: Double) = viewModelScope.launch {
        teachers.rateTeacher(teacherUid = teacherData.value!!.teacherId, rate)
        showToast.value = "Rate Saved"
    }
}