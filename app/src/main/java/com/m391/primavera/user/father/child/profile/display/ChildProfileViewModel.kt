package com.m391.primavera.user.father.child.profile.display

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.NavigationCommand
import com.m391.primavera.utils.models.ServerChildModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChildProfileViewModel(
    app: Application,
) : BaseViewModel(app) {
    private val dataStoreManager: DataStoreManager =
        DataStoreManager.getInstance(app.applicationContext)
    private val serverDatabase: ServerDatabase =
        ServerDatabase(app.applicationContext, dataStoreManager)
    private val _childInfo = MutableLiveData<ServerChildModel>()
    val childInfo: LiveData<ServerChildModel> = _childInfo
    private val children = serverDatabase.childInformation
    private val _childAge = MutableLiveData<String>()
    val childAge: LiveData<String> = _childAge

    private val auth = serverDatabase.authentication
    private val conversations = serverDatabase.conversations
    fun backToTeacherSearch() {
        navigationCommand.value = NavigationCommand.Back
    }

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

    suspend fun createConversation(uid: String) =
        withContext(Dispatchers.IO) {
            conversations.createTeacherWithFatherConversation(uid)
        }

    fun checkChildFather(fatherUid: String): Boolean {
        return auth.getCurrentUser()!!.uid == fatherUid
    }
}