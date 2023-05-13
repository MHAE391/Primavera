package com.m391.primavera.chat

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import kotlinx.coroutines.launch

class ChatActivityViewModel(app: Application) : BaseViewModel(app) {

    private val dataStore = DataStoreManager.getInstance(app.applicationContext)
    private val auth = ServerDatabase(app.applicationContext, dataStore).authentication
    val _senderUid = MutableLiveData<String>()
    val _receiverUid = MutableLiveData<String>()
    fun setChatSenderAndReceiver(uid: String) {
        viewModelScope.launch {
            _receiverUid.value = uid
            _senderUid.value = auth.getCurrentUser()!!.uid
        }
    }
}