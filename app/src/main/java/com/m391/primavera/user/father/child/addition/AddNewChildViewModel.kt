package com.m391.primavera.user.father.child.addition

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.NavigationCommand
import com.m391.primavera.utils.capitalize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddNewChildViewModel(
    app: Application,
) : BaseViewModel(app) {
    val childWatch = MutableLiveData<String>()
    val childDateOfBarth = MutableLiveData<String>()
    private val childWatchAddress = MutableLiveData<String>()
    val childName = MutableLiveData<String>()
    private val childAcademicYear = MutableLiveData<String>()
    val childImage = MutableLiveData<String>()
    private val dataStoreManager: DataStoreManager =
        DataStoreManager.getInstance(app.applicationContext)
    private val serverDatabase: ServerDatabase =
        ServerDatabase(app.applicationContext, dataStoreManager)
    private val father = serverDatabase.fatherInformation

    init {
        childWatch.value = "Scan QR Code"
        childDateOfBarth.value = "Date Of Barth"
    }

    fun backToFatherProfile() {
        navigationCommand.value = NavigationCommand.Back
    }

    fun getChildWatch(name: String) {
        childWatch.value = name
        childWatchAddress.value = name
    }

    fun getChildAcademicYear(year: String) {
        childAcademicYear.value = year
    }

    suspend fun createNewChild(): String = withContext(Dispatchers.Main) {
        showLoading.value = true
        var response = SUCCESS
        response = if (validateEnteredData() == "Complete Data") {
            father.addNewChild(
                childName = capitalize(childName.value!!.trim()),
                childAcademicYear = childAcademicYear.value!!,
                childDateOfBarth = childDateOfBarth.value!!,
                childImage = childImage.value!!,
                watchUid = childWatch.value!!
            )
        } else validateEnteredData()
        showLoading.value = false
        return@withContext response
    }

    private suspend fun validateEnteredData(): String = withContext(Dispatchers.IO) {
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