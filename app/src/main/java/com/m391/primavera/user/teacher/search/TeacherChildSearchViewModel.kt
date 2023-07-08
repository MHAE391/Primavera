package com.m391.primavera.user.teacher.search

import android.app.Application
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.NavigationCommand
import com.m391.primavera.utils.models.ServerChildModel
import com.m391.primavera.utils.models.ServerTeacherModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class TeacherChildSearchViewModel(app: Application) : BaseViewModel(app) {

    private val dataStore = DataStoreManager.getInstance(app.applicationContext)
    private val children = ServerDatabase(app.applicationContext, dataStore).childInformation
    private val _childrenList = MutableLiveData<List<ServerChildModel>>()
    private val conversations = ServerDatabase(app.applicationContext, dataStore).conversations
    val childrenList: LiveData<List<ServerChildModel>> = _childrenList
    private val childrenArray = ArrayList<ServerChildModel>()
    private val auth = ServerDatabase(app.applicationContext, dataStore).authentication
    fun backToConversations() {
        navigationCommand.value = NavigationCommand.Back
    }

    suspend fun openStreamChildren(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        children.streamChildren().observe(lifecycleOwner) {
            it?.forEach { serverChild ->
                if (!childrenArray.contains(serverChild)) childrenArray.add(serverChild)
            }
            _childrenList.value = childrenArray
            validateData()
        }
        validateData()
    }


    suspend fun closeStreamChildren(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        children.streamChildren().removeObservers(lifecycleOwner)
        _childrenList.removeObservers(lifecycleOwner)
        withContext(Dispatchers.IO) {
            children.closeChildStream()
        }
    }

    val searchWatcher = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(text: String?): Boolean {
            val searchChildArray = ArrayList<ServerChildModel>()
            childrenArray.forEach { storedChild ->
                if (text != null) {
                    if (storedChild.childName.contains(text.toRegex())
                        || storedChild.academicYear.contains(text.toRegex())
                    ) {
                        searchChildArray.add(storedChild)
                        _childrenList.value = searchChildArray
                        validateData()
                    }
                }
            }
            return true
        }

        override fun onQueryTextChange(text: String?): Boolean {
            val searchChildArray = ArrayList<ServerChildModel>()
            childrenArray.forEach { storedChild ->
                if (text != null) {
                    if (storedChild.childName.lowercase().contains(text.lowercase().toRegex())
                        || storedChild.academicYear.lowercase().contains(text.lowercase().toRegex())
                    ) {
                        searchChildArray.add(storedChild)
                        _childrenList.value = searchChildArray
                        validateData()
                    }
                }
            }
            _childrenList.value = searchChildArray
            validateData()
            return true
        }
    }

    fun checkChildFather(fatherUid: String): Boolean {
        return auth.getCurrentUser()!!.uid == fatherUid
    }

    suspend fun createConversation(uid: String) =
        withContext(Dispatchers.IO) {
            conversations.createTeacherWithFatherConversation(uid)
        }

    private fun validateData() {
        showNoData.value = _childrenList.value.isNullOrEmpty()
    }
}