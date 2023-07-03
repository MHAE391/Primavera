package com.m391.primavera.authentication.information.teacher

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.capitalize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.security.auth.Subject

class TeacherInformationViewModel(private val app: Application) : BaseViewModel(app) {
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
    val teacherFirstName = MutableLiveData<String>()
    val teacherLastName = MutableLiveData<String>()
    val teacherDateOfBarth = MutableLiveData<String>()
    val teacherImage = MutableLiveData<String>()
    private val dataStoreManager = DataStoreManager.getInstance(app.applicationContext)
    private val teachers =
        ServerDatabase(context = app.applicationContext, dataStoreManager).teacherInformation
    private val _response = MutableLiveData<String?>()
    val response: LiveData<String?> = _response

    init {
        selectAcademicYears.value = "Select Your Academic Years"
        selectAcademicSubjects.value = "Select Your Academic Subjects"
        selectTeacherLocation.value = "Select Teacher Location"
        teacherDateOfBarth.value = "Select Your Date Of Barth"
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

    suspend fun setData() = withContext(Dispatchers.Main) {
        showLoading.value = true
        if (validateEnteredData() == "Complete Data") {
            _response.value = teachers.uploadTeacher(
                teacherFirstName = capitalize(teacherFirstName.value!!.trim()),
                teacherLastName = capitalize(teacherLastName.value!!.trim()),
                teacherAcademicYears = selectedAcademicYears.value!!,
                teacherDateOfBarth = teacherDateOfBarth.value!!,
                teacherImage = teacherImage.value!!,
                latitude = selectedLatitude.value!!,
                longitude = selectedLongitude.value!!,
                subjects = selectedAcademicSubjects.value!!
            )
        } else showToast.value = validateEnteredData()
        showLoading.value = false
    }

    private suspend fun validateEnteredData(): String = withContext(Dispatchers.IO) {
        return@withContext if (teacherFirstName.value.isNullOrBlank() || teacherLastName.value.isNullOrBlank())
            "Complete Teacher Information"
        else if (teacherImage.value.isNullOrBlank())
            "Please, Choose Teacher Image"
        else if (selectedAcademicSubjects.value.isNullOrEmpty())
            "Please, Select Teacher Academic Years"
        else if (selectedAcademicSubjects.value.isNullOrEmpty())
            "Please, Select Teacher Academic Subjects"
        else if (selectTeacherLocation.value == "Select Teacher Location")
            "Please, Select Your Location"
        else if (teacherDateOfBarth.value == "Select Your Date Of Barth")
            "Please, Select  Your Date Of Barth"
        else "Complete Data"
    }

    fun setLocationString(location: String) {
        selectTeacherLocation.value = location
    }

}