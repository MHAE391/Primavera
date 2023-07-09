package com.m391.primavera.chat

import android.app.Application
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.notification.Notification
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.Constants.IMAGE_MESSAGE
import com.m391.primavera.utils.Constants.TEXT_MESSAGE
import com.m391.primavera.utils.Constants.VOICE_MESSAGE
import com.m391.primavera.utils.models.ServerMessageModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivityViewModel(app: Application) : BaseViewModel(app) {

    private val dataStore = DataStoreManager.getInstance(app.applicationContext)
    private val auth = ServerDatabase(app.applicationContext, dataStore).authentication
    private val _senderUid = MutableLiveData<String>()
    private val _receiverUid = MutableLiveData<String>()
    private val _serverMessages = MutableLiveData<List<ServerMessageModel>>()
    private val messaging = ServerDatabase(app.applicationContext, dataStore).messageInformation
    val serverMessages: LiveData<List<ServerMessageModel>> = _serverMessages
    private val _receiverFirstName = MutableLiveData<String>()
    val receiverFirstName: LiveData<String> = _receiverFirstName
    fun setChatSenderAndReceiver(uid: String, firstName: String) {
        viewModelScope.launch {
            _receiverUid.value = uid
            _receiverFirstName.value = firstName
            _senderUid.value = auth.getCurrentUser()!!.uid
            dataStore.setCurrentChatReceiver(uid)
        }
    }

    suspend fun removeChatFromDatastore() {
        withContext(Dispatchers.IO) {
            dataStore.setCurrentChatReceiver(null)
        }
    }

    suspend fun openMessagesStream(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        messaging.streamMessages(_senderUid.value!! + _receiverUid.value!!)
            .observe(lifecycleOwner, Observer {
                _serverMessages.postValue(it)
            })

    }

    val messageText = MutableLiveData<String?>()

    suspend fun sendTextMessage(): String = withContext(Dispatchers.IO) {
        return@withContext messaging.sendMessage(
            messageType = TEXT_MESSAGE,
            messageContent = messageText.value!!,
            receiverUID = _receiverUid.value!!,
            senderUID = _senderUid.value!!
        )

    }

    suspend fun closeMessagesStream(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        messaging.streamMessages(_senderUid.value!! + _receiverUid.value!!)
            .removeObservers(lifecycleOwner)
        withContext(Dispatchers.IO) {
            messaging.closeMessagesStream()
        }
    }

    suspend fun sendImageMessage(uri: String): String = withContext(Dispatchers.IO) {
        return@withContext messaging.sendMessage(
            messageType = IMAGE_MESSAGE,
            messageContent = uri,
            receiverUID = _receiverUid.value!!,
            senderUID = _senderUid.value!!
        )
    }

    suspend fun sendAudioMessage(uri: String): String = withContext(Dispatchers.IO) {
        return@withContext messaging.sendMessage(
            messageType = VOICE_MESSAGE,
            messageContent = uri,
            receiverUID = _receiverUid.value!!,
            senderUID = _senderUid.value!!
        )
    }

    suspend fun sendFCM(messageType: String) = withContext(Dispatchers.IO) {
        when (messageType) {
            TEXT_MESSAGE -> {
                Notification().sendMessageFCMToFatherOrTeacher(
                    "user${_receiverUid.value}",
                    messageType,
                    auth.getCurrentUser()!!.displayName!!,
                    messageText.value!!,
                    auth.getCurrentUser()!!.uid
                )
                messageText.postValue(null)
            }

            VOICE_MESSAGE -> {
                Notification().sendMessageFCMToFatherOrTeacher(
                    "user${_receiverUid.value}",
                    messageType,
                    auth.getCurrentUser()!!.displayName!!,
                    "Send You Voice Message",
                    auth.getCurrentUser()!!.uid
                )
            }

            IMAGE_MESSAGE -> {
                Notification().sendMessageFCMToFatherOrTeacher(
                    "user${_receiverUid.value}",
                    messageType,
                    auth.getCurrentUser()!!.displayName!!,
                    "Send You Image",
                    auth.getCurrentUser()!!.uid
                )
            }
        }

    }
}