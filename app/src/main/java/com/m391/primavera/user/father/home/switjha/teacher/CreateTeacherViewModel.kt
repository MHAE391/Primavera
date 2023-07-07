package com.m391.primavera.user.father.home.switjha.teacher

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.NavigationCommand
import com.m391.primavera.utils.models.ServerFatherModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateTeacherViewModel(app: Application) : BaseViewModel(app) {
    private val dataStoreManager: DataStoreManager =
        DataStoreManager.getInstance(app.applicationContext)
    private val serverDatabase: ServerDatabase =
        ServerDatabase(app.applicationContext, dataStoreManager)

    private val fathers = serverDatabase.fatherInformation
    private val teachers = serverDatabase.teacherInformation
    private val _fatherInfo = MutableLiveData<ServerFatherModel>()
    val fatherInfo: LiveData<ServerFatherModel> = _fatherInfo
    val selectAcademicYears = MutableLiveData<String>()
    val selectAcademicSubjects = MutableLiveData<String>()
    val selectTeacherLocation = MutableLiveData<String>()
    private val _selectedAcademicYears = MutableLiveData<List<String>>()
    private val _selectedAcademicSubjects = MutableLiveData<List<String>>()
    val selectedAcademicYears: LiveData<List<String>> = _selectedAcademicYears
    val selectedAcademicSubjects: MutableLiveData<List<String>> = _selectedAcademicSubjects
    val selectedLatitude = MutableLiveData<Number>()
    val selectedLongitude = MutableLiveData<Number>()
    private val subjects = HashSet<String>()
    private val years = HashSet<String>()
    val teacherDateOfBarth = MutableLiveData<String>()

    init {
        teacherDateOfBarth.value = "Date Of Barth"
        selectTeacherLocation.value = "Select Your Location"
        selectAcademicSubjects.value = "Select Subjects"
        selectAcademicYears.value = "Select Years"
    }

    fun backToFatherHome() {
        navigationCommand.value = NavigationCommand.Back
    }

    fun setLocation(longitude: Number, latitude: Number) {
        selectedLatitude.value = latitude
        selectedLongitude.value = longitude
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

    fun setLocationString(location: String) {
        selectTeacherLocation.value = location
    }

    suspend fun openStream(lifecycleOwner: LifecycleOwner, uid: String) =
        withContext(Dispatchers.Main) {
            fathers.streamFatherInformationByUID(uid).observe(lifecycleOwner) {
                _fatherInfo.postValue(it)
            }
        }

    suspend fun closeStream(lifecycleOwner: LifecycleOwner, uid: String) = viewModelScope.launch {
        fathers.streamFatherInformationByUID(uid).removeObservers(lifecycleOwner)
        withContext(Dispatchers.IO) {
            fathers.closeFatherStream()
        }
    }

    private suspend fun validateEnteredData(): String = withContext(Dispatchers.IO) {
        return@withContext if (selectedAcademicYears.value.isNullOrEmpty())
            "Please, Select Teacher Academic Years"
        else if (selectedAcademicSubjects.value.isNullOrEmpty())
            "Please, Select Teacher Academic Subjects"
        else if (selectedLatitude.value == null && selectedLongitude.value == null)
            "Please, Select Your Location"
        else if (teacherDateOfBarth.value == "Date Of Barth")
            "Please, Select  Your Date Of Barth"
        else "Complete Data"
    }

    suspend fun createTeacherAccount(): String = withContext(Dispatchers.Main) {
        var response = SUCCESS
        showLoading.value = true
        response = validateEnteredData()
        if (response == "Complete Data") {
            response = fathers.createTeacherAccount(
                teacherFirstName = fatherInfo.value!!.firstName,
                teacherLastName = fatherInfo.value!!.lastName,
                teacherDateOfBarth = teacherDateOfBarth.value!!,
                teacherAcademicYears = selectedAcademicYears.value!!,
                teacherImagePath = fatherInfo.value!!.image.toString(),
                teacherImageUri = fatherInfo.value!!.imageUri,
                latitude = selectedLatitude.value!!,
                longitude = selectedLongitude.value!!,
                subjects = selectedAcademicSubjects.value!!,
            )
        }
        showLoading.value = false
        return@withContext response
    }

}