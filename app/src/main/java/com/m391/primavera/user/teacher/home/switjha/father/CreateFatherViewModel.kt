package com.m391.primavera.user.teacher.home.switjha.father

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.NavigationCommand
import com.m391.primavera.utils.capitalize
import com.m391.primavera.utils.models.ServerTeacherModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateFatherViewModel(app: Application) : BaseViewModel(app) {
    private val dataStoreManager = DataStoreManager.getInstance(app.applicationContext)
    private val serverDatabase = ServerDatabase(app.applicationContext, dataStoreManager)
    private val fathers = serverDatabase.fatherInformation
    private val teachers = serverDatabase.teacherInformation
    private val children = serverDatabase.childInformation
    private val _teacherInfo = MutableLiveData<ServerTeacherModel>()
    val teacherInfo: LiveData<ServerTeacherModel> = _teacherInfo
    val auth = serverDatabase.authentication
    val childName = MutableLiveData<String>()
    val childWatch = MutableLiveData<String>()
    val childDateOfBarth = MutableLiveData<String>()
    val childImage = MutableLiveData<String>()
    val fatherLocation = MutableLiveData<String>()
    private val childAcademicYear = MutableLiveData<String>()
    private val _selectedLatitude = MutableLiveData<Number>()
    val selectedLatitude: LiveData<Number> = _selectedLatitude
    private val _selectedLongitude = MutableLiveData<Number>()
    val selectedLongitude: LiveData<Number> = _selectedLongitude


    init {
        childWatch.value = "Scan QR Code"
        childDateOfBarth.value = "Date Of Barth"
        fatherLocation.value = "Select Location"
    }

    fun getChildWatch(name: String) {
        childWatch.value = name
    }

    fun getChildAcademicYear(year: String) {
        childAcademicYear.value = year
    }

    suspend fun openStream(lifecycleOwner: LifecycleOwner) = withContext(Dispatchers.Main) {
        teachers.streamTeacherByUid(auth.getCurrentUser()!!.uid)
            .observe(lifecycleOwner, Observer { serverTeacher ->
                _teacherInfo.value = serverTeacher
                _selectedLatitude.value = serverTeacher.latitude
                _selectedLongitude.value = serverTeacher.longitude
            })
    }


    fun closeStream(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        teachers.streamTeacherByUid(auth.getCurrentUser()!!.uid).removeObservers(lifecycleOwner)
        withContext(Dispatchers.IO) {
            teachers.closeUsersStream()
        }
    }

    fun backToTeacherHome() {
        navigationCommand.value = NavigationCommand.Back
    }

    fun setLocation(longitude: Number, latitude: Number) {
        _selectedLatitude.value = latitude
        _selectedLongitude.value = longitude
    }

    fun setLocationString(name: String) {
        fatherLocation.value = name
    }

    suspend fun createFatherAccount(): String = withContext(Dispatchers.Main) {
        showLoading.value = true
        var response = SUCCESS
        response = validateData()
        if (response == "Complete Data") {
            response = fathers.createFatherAccount(
                fatherFirstName = teacherInfo.value!!.firstName,
                fatherLastName = teacherInfo.value!!.lastName,
                fatherImagePath = teacherInfo.value!!.image.toString(),
                fatherImageUri = teacherInfo.value!!.imageUri,
                latitude = selectedLatitude.value!!,
                longitude = selectedLongitude.value!!,
                fatherPhone = teacherInfo.value!!.phone,
                childName = capitalize(childName.value!!.trim()),
                watchUid = childWatch.value!!,
                childImage = childImage.value!!,
                childDateOfBarth = childDateOfBarth.value!!,
                childAcademicYear = childAcademicYear.value!!
            )
        }
        showLoading.value = false
        return@withContext response
    }

    private suspend fun validateData(): String = withContext(Dispatchers.IO) {
        return@withContext if (childName.value.isNullOrBlank())
            "Enter Child Name"
        else if (childWatch.value == "Scan QR Code")
            "Please, Select Your Child Watch"
        else if (childAcademicYear.value == "None")
            "Select Your Child Academic Year"
        else if (childDateOfBarth.value == "Date Of Barth")
            "Please, Select Child Date Of Barth"
        else if (childImage.value == null)
            "Please, Select Child Image"
        else "Complete Data"
    }
}