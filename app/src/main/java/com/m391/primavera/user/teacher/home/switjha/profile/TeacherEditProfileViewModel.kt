package com.m391.primavera.user.teacher.home.switjha.profile

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.Constants.FATHER
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.Constants.TEACHER
import com.m391.primavera.utils.NavigationCommand
import com.m391.primavera.utils.models.ServerTeacherModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class TeacherEditProfileViewModel(app: Application) : BaseViewModel(app) {

    private val dataStoreManager = DataStoreManager.getInstance(app.applicationContext)
    private val serverDatabase = ServerDatabase(app.applicationContext, dataStoreManager)
    private val _teacherInfo = MutableLiveData<ServerTeacherModel>()
    val teacherInfo: LiveData<ServerTeacherModel> = _teacherInfo
    private val _selectedAcademicYears = MutableLiveData<List<String>>()
    val selectedAcademicYears: LiveData<List<String>> = _selectedAcademicYears
    private val _selectedAcademicSubjects = MutableLiveData<List<String>>()
    val selectedAcademicSubjects: LiveData<List<String>> = _selectedAcademicSubjects
    private val teachers = serverDatabase.teacherInformation
    private val _selectedLongitude = MutableLiveData<Number>()
    val selectedLongitude: LiveData<Number> = _selectedLongitude
    private val fathers = serverDatabase.fatherInformation
    private val _selectedLatitude = MutableLiveData<Number>()
    val selectedLatitude: LiveData<Number> = _selectedLatitude

    private val messageInformation = serverDatabase.messageInformation
    private val _teacherRate = MutableLiveData<Number>()
    val teacherRate: LiveData<Number> = _teacherRate

    private val subjects = HashSet<String>()
    private val years = HashSet<String>()
    private val _teacherAge = MutableLiveData<String>()
    val teacherAge: LiveData<String> = _teacherAge
    val auth = serverDatabase.authentication
    suspend fun openStream(lifecycleOwner: LifecycleOwner) = withContext(Dispatchers.Main) {
        teachers.streamTeacherByUid(auth.getCurrentUser()!!.uid)
            .observe(lifecycleOwner, Observer { serverTeacher ->
                _selectedAcademicYears.value = serverTeacher.academicYears
                _selectedAcademicSubjects.value = serverTeacher.subjects
                subjects.addAll(selectedAcademicSubjects.value!!)
                years.addAll(selectedAcademicYears.value!!)
                _selectedLongitude.value = serverTeacher.longitude
                _selectedLatitude.value = serverTeacher.latitude
                _teacherAge.value = calculateAge(serverTeacher.dateOfBarth).toString()
                _teacherInfo.value = serverTeacher
            })
    }


    fun closeStream(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        if (auth.getCurrentUser() != null) {
            teachers.streamTeacherByUid(auth.getCurrentUser()!!.uid).removeObservers(lifecycleOwner)
            withContext(Dispatchers.IO) {
                teachers.closeUsersStream()
            }
        }
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

    fun setLocation(longitude: Number, latitude: Number) {
        _selectedLatitude.value = latitude
        _selectedLongitude.value = longitude
    }

    fun addSubject(subject: String) {
        subjects.add(subject)
        _selectedAcademicSubjects.value = subjects.toList()
    }

    fun removeSubject(subject: String) {
        subjects.remove(subject)
        _selectedAcademicSubjects.value = subjects.toList()
    }

    fun addYear(year: String) {
        years.add(year)
        _selectedAcademicYears.value = years.toList()
    }

    fun removeYear(year: String) {
        years.remove(year)
        _selectedAcademicYears.value = years.toList()
    }

    fun backToHome() {
        navigationCommand.value = NavigationCommand.Back
    }

    suspend fun updateTeacherInformation(): String = withContext(Dispatchers.Main) {
        var response = SUCCESS
        showLoading.value = true
        response = checkData()
        if (response == "Update") {
            response = teachers.updateTeacherInformation(
                image = teacherNewImage.value ?: "No New Image",
                longitude = selectedLongitude.value!!,
                latitude = selectedLatitude.value!!,
                academicYears = selectedAcademicYears.value!!,
                subjects = selectedAcademicSubjects.value!!
            )
        }
        showLoading.value = false
        return@withContext response
    }

    val teacherNewImage = MutableLiveData<String>()
    private suspend fun checkData(): String = withContext(Dispatchers.IO) {
        return@withContext if (teacherNewImage.value == null
            && selectedLatitude.value == teacherInfo.value!!.latitude
            && selectedLongitude.value == teacherInfo.value!!.longitude
            && selectedAcademicYears.value == teacherInfo.value!!.academicYears
            && selectedAcademicSubjects.value == teacherInfo.value!!.subjects
        ) "Nothing to change"
        else "Update"
    }

    suspend fun deleteTeacherAccount(lifecycleOwner: LifecycleOwner): String =
        withContext(Dispatchers.Main) {
            showLoading.value = true
            withContext(Dispatchers.IO) {
                teachers.closeUsersStream()
            }
            val fatherCheck = fathers.checkAlreadyFatherOrNot()
            if (fatherCheck) {
                dataStoreManager.setUserType(FATHER)
                if (dataStoreManager.getCurrentChildUid() == null) dataStoreManager.setCurrentChildUid(
                    fathers.getRandomChildUID()
                )
                return@withContext FATHER
            } else {
                dataStoreManager.setUserType(null)
                dataStoreManager.setCurrentChildUid(null)
                dataStoreManager.setUserUid(null)
            }
            return@withContext TEACHER
        }

    suspend fun deleteAccount(): String = withContext(Dispatchers.IO) {
        withContext(Dispatchers.IO) {
            teachers.closeUsersStream()
        }
        return@withContext fathers.deleteMyTeacherAccount()
    }

    suspend fun logOut(): String = withContext(Dispatchers.IO) {
        withContext(Dispatchers.IO) {
            teachers.closeUsersStream()
            messageInformation.unSubscribeToTopic()
        }
        val response = teachers.deleteTeacherAccount()
        auth.logOut()
        return@withContext response
    }
}