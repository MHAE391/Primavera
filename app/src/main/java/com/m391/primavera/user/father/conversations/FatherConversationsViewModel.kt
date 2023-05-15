package com.m391.primavera.user.father.conversations

import android.app.Application
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.NavigationCommand
import com.m391.primavera.utils.models.ServerConversationModel
import com.m391.primavera.utils.models.ServerTeacherModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class FatherConversationsViewModel(app: Application) : BaseViewModel(app) {
    val dataStore = DataStoreManager.getInstance(app.applicationContext)
    val conversations = ServerDatabase(app.applicationContext, dataStore).conversations

    private val _conversationsList = MutableLiveData<List<ServerConversationModel>>()
    val conversationsList: LiveData<List<ServerConversationModel>> = _conversationsList
    val conversationsArrayList = kotlin.collections.ArrayList<ServerConversationModel>()
    suspend fun openStreamConversations(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        conversations.streamConversations().observe(lifecycleOwner, Observer {
            _conversationsList.postValue(it)
            it?.forEach { s ->
                conversationsArrayList.add(s)
            }
        })
    }

    fun backToHome() {
        navigationCommand.value = NavigationCommand.Back
    }

    suspend fun closeStreamConversation(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        conversations.streamConversations().removeObservers(lifecycleOwner)
        withContext(Dispatchers.IO) {
            conversations.closeConversationsStream()
        }
    }

    val searchWatcher = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(text: String?): Boolean {

            return true
        }

        override fun onQueryTextChange(text: String?): Boolean {
            val conversationsArray = kotlin.collections.ArrayList<ServerConversationModel>()
            conversationsArrayList.forEach { storedTeacher ->
                if (text != null) {
                    if (storedTeacher.firstName.lowercase(Locale.getDefault()).contains(
                            text.lowercase(Locale.getDefault()).toRegex()
                        )
                        || storedTeacher.lastName.lowercase(Locale.getDefault())
                            .contains(text.lowercase(Locale.getDefault()).toRegex())
                    ) {
                        conversationsArray.add(storedTeacher)
                        _conversationsList.value = conversationsArray
                    }
                }
            }
            _conversationsList.value = conversationsArray

            return true
        }
    }
}