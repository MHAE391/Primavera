package com.m391.primavera.authentication.information.father

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.capitalize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FatherInformationViewModel(private val app: Application) : BaseViewModel(app) {
    val childWatch = MutableLiveData<String>()
    val fatherImage = MutableLiveData<String>()
    val childImage = MutableLiveData<String>()
    private val childWatchAddress = MutableLiveData<String>()
    val fatherFirstName = MutableLiveData<String>()
    val fatherLastName = MutableLiveData<String>()
    val childName = MutableLiveData<String>()
    val childAge = MutableLiveData<String>()
    private val childAcademicYear = MutableLiveData<String>()
    private val serverDatabase = ServerDatabase(context = app.applicationContext).fatherInformation
    private val _response = MutableLiveData<String?>()
    val response: LiveData<String?> = _response

    init {
        childWatch.value = "Select Your Child Watch"
    }

    fun getChildWatch(name: String, macAddress: String) {
        childWatch.value = name
        childWatchAddress.value = macAddress
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
                childAge = childAge.value!!,
                childImage = childImage.value!!,
                watchUid = childWatchAddress.value!!,
                childAcademicYear = childAcademicYear.value!!
            )
        } else showToast.value = validateEnteredData()
        showLoading.value = false
    }

    private suspend fun validateEnteredData(): String = withContext(Dispatchers.IO) {
        return@withContext if (fatherFirstName.value.isNullOrBlank() || fatherLastName.value.isNullOrBlank())
            "Complete Father Information"
        else if (fatherImage.value.isNullOrBlank())
            "Please, Choose Father Image"
        else if (childAge.value.isNullOrBlank() || childName.value.isNullOrBlank())
            "Complete Child Information"
        else if (childWatch.value == "Select Your Child Watch")
            "Please, Select Your Child Watch"
        else if (childAcademicYear.value == "None")
            "Select Your Child Academic Year"
        else "Complete Data"
    }
}