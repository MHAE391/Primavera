package com.m391.primavera.authentication.information.father

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.capitalize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FatherInformationViewModel(private val app: Application) : BaseViewModel(app) {
    val childWatch = MutableLiveData<String>()
    val childDateOfBarth = MutableLiveData<String>()
    private val childWatchAddress = MutableLiveData<String>()
    val fatherImage = MutableLiveData<String>()
    val childImage = MutableLiveData<String>()
    val fatherLocation = MutableLiveData<String>()
    val fatherFirstName = MutableLiveData<String>()
    val fatherLastName = MutableLiveData<String>()
    val childName = MutableLiveData<String>()
    private val childAcademicYear = MutableLiveData<String>()
    private val dataStoreManager = DataStoreManager.getInstance(app.applicationContext)
    private val serverDatabase =
        ServerDatabase(context = app.applicationContext, dataStoreManager).fatherInformation
    private val _response = MutableLiveData<String?>()
    val response: LiveData<String?> = _response

    val selectedLatitude = MutableLiveData<Number>()
    val selectedLongitude = MutableLiveData<Number>()

    fun setLocation(longitude: Number, latitude: Number) {
        selectedLatitude.value = latitude
        selectedLongitude.value = longitude
    }

    fun setLocationString(location: String) {
        fatherLocation.value = location
    }

    init {
        childWatch.value = "Select Your Child Watch"
        fatherLocation.value = "Select Your Location"
        childDateOfBarth.value = "Select Child Date Of Barth"
    }


    fun getChildWatch(name: String) {
        childWatch.value = name
        childWatchAddress.value = name
    }

    fun getChildAcademicYear(year: String) {
        childAcademicYear.value = year
    }

    suspend fun setData() = withContext(Dispatchers.Main) {
        showLoading.value = true
        if (validateEnteredData() == "Complete Data") {
            _response.value = serverDatabase.uploadFather(
                fatherFirstName = capitalize(fatherFirstName.value!!.trim()),
                fatherLastName = capitalize(fatherLastName.value!!.trim()),
                fatherImage = fatherImage.value!!,
                childName = capitalize(childName.value!!.trim()),
                childDateOfBarth = childDateOfBarth.value!!,
                childImage = childImage.value!!,
                watchUid = childWatchAddress.value!!,
                childAcademicYear = childAcademicYear.value!!,
                latitude = selectedLatitude.value!!,
                longitude = selectedLongitude.value!!
            )
        } else showToast.value = validateEnteredData()
        showLoading.value = false
    }

    private suspend fun validateEnteredData(): String = withContext(Dispatchers.IO) {
        return@withContext if (fatherFirstName.value.isNullOrBlank() || fatherLastName.value.isNullOrBlank())
            "Complete Father Information"
        else if (fatherImage.value.isNullOrBlank())
            "Please, Choose Father Image"
        else if (childName.value.isNullOrBlank())
            "Complete Child Information"
        else if (childWatch.value == "Select Your Child Watch")
            "Please, Select Your Child Watch"
        else if (childAcademicYear.value == "None")
            "Select Your Child Academic Year"
        else if (fatherLocation.value == "Select Your Location")
            "Please, Select Your Location"
        else if (childDateOfBarth.value == "Select Child Date Of Barth")
            "Please, Select Child Date Of Barth"
        else "Complete Data"
    }
}