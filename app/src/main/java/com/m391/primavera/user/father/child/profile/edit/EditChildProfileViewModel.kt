package com.m391.primavera.user.father.child.profile.edit

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
import com.m391.primavera.utils.models.ServerChildModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditChildProfileViewModel(
    app: Application
) : BaseViewModel(app) {
    private val dataStoreManager: DataStoreManager =
        DataStoreManager.getInstance(app.applicationContext)
    private val serverDatabase: ServerDatabase =
        ServerDatabase(app.applicationContext, dataStoreManager)
    private val childNewAcademicYear = MutableLiveData<String>()
    val childNewWatch = MutableLiveData<String>()
    val childNewImage = MutableLiveData<String>()
    private val _childInfo = MutableLiveData<ServerChildModel>()
    val childInfo: LiveData<ServerChildModel> = _childInfo
    private val children = serverDatabase.childInformation
    private val _childAge = MutableLiveData<String>()
    val childAge: LiveData<String> = _childAge

    private val fathers = serverDatabase.fatherInformation
    suspend fun openStream(lifecycleOwner: LifecycleOwner, childUid: String) =
        withContext(Dispatchers.Main) {
            children.streamChildInformationByUID(childUid).observe(lifecycleOwner) {
                _childInfo.postValue(it)
                _childAge.postValue(calculateAge(it.dateOfBarth).toString())
            }
        }

    suspend fun closeStream(lifecycleOwner: LifecycleOwner, childUid: String) =
        viewModelScope.launch {
            children.streamChildInformationByUID(childUid).removeObservers(lifecycleOwner)
            withContext(Dispatchers.IO) {
                children.closeChildStream()
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

    fun backToFatherHome() {
        navigationCommand.value = NavigationCommand.Back
    }

    fun getChildAcademicYear(year: String) {
        childNewAcademicYear.value = year
    }

    suspend fun updateChildInformation(): String = withContext(Dispatchers.Main) {
        showLoading.value = true
        var response = SUCCESS
        if (childNewImage.value == null && childNewWatch.value == null && childNewAcademicYear.value == null) {
            showToast.value = "Nothing to change"
            response = "Nothing to change"
        } else {
            response = children.updateChildInformation(
                watchUid = childNewWatch.value ?: "No New Watch",
                image = childNewImage.value ?: "No New Image",
                academicYear = childNewAcademicYear.value ?: "No New Academic Year",
                childUid = childInfo.value!!.childUID,
                fatherName = childInfo.value!!.fatherName,
                fatherUid = childInfo.value!!.fatherUID,
                childName = childInfo.value!!.childName
            )
        }
        showLoading.value = false

        return@withContext response
    }

    suspend fun deleteChildFromFatherList(): String =
        withContext(Dispatchers.Main) {
            var response = SUCCESS
            showLoading.value = true
            response = if (fathers.getFatherChildrenNumber() <= 1) {
                "You have only one child so can't delete this child"
            } else {
                if (childInfo.value!!.childUID != dataStoreManager.getCurrentChildUid()) {
                    fathers.deleteChild(childUid = childInfo.value!!.childUID)
                } else {
                    "You  Can't delete current logged in Child"
                }
            }
            showLoading.value = false
            return@withContext response
        }

    suspend fun deleteChildInformation() = withContext(Dispatchers.IO) {
        children.deleteChild(childInfo.value!!.childUID)
    }
}