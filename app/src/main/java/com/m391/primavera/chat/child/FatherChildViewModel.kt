package com.m391.primavera.chat.child

import android.app.Application
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.m391.primavera.App
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.notification.Notification
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.models.ServerMessageModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FatherChildViewModel(app: Application) : BaseViewModel(app) {

    fun startTimer(textView: TextView) {
        startTime = System.currentTimeMillis()
        isTimerRunning = true
        updateTimer(textView)
    }

    fun stopTimer(textView: TextView) {
        isTimerRunning = false
        timerRunnable?.let { textView.removeCallbacks(it) }
        timerRunnable = null
    }

    private fun updateTimer(textView: TextView) {
        if (isTimerRunning) {
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - startTime
            val formattedTime = getFormattedTime(elapsedTime)
            textView.text = formattedTime
            timerRunnable = Runnable { updateTimer(textView) }
            textView.postDelayed(timerRunnable, 1000) // Update every second (1000 milliseconds)
        }
    }

    private var startTime: Long = 0
    private var isTimerRunning: Boolean = false
    private var timerRunnable: Runnable? = null
    private fun getFormattedTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        val hours = (milliseconds / (1000 * 60 * 60)) % 24
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }


    private val _receiverFirstName = MutableLiveData<String>()
    val receiverFirstName: LiveData<String> = _receiverFirstName
    private val dataStore = DataStoreManager.getInstance(app.applicationContext)
    private val auth = ServerDatabase(app.applicationContext, dataStore).authentication
    private val _senderUid = MutableLiveData<String>()
    private val _receiverUid = MutableLiveData<String>()
    private val _serverMessages = MutableLiveData<List<ServerMessageModel>>()
    private val messaging = ServerDatabase(app.applicationContext, dataStore).messageInformation
    private val watches = ServerDatabase(app.applicationContext, dataStore).watches
    private val children = ServerDatabase(app.applicationContext, dataStore).childInformation
    val serverMessages: LiveData<List<ServerMessageModel>> = _serverMessages

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

    suspend fun sendAudioMessage(uri: String): String = withContext(Dispatchers.IO) {
        return@withContext messaging.sendMessage(
            messageType = Constants.VOICE_MESSAGE,
            messageContent = uri,
            receiverUID = _receiverUid.value!!,
            senderUID = _senderUid.value!!
        )
    }

    suspend fun openMessagesStream(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        messaging.streamMessages(_senderUid.value!! + _receiverUid.value!!)
            .observe(lifecycleOwner) {
                _serverMessages.postValue(it)
            }
    }

    suspend fun closeMessagesStream(lifecycleOwner: LifecycleOwner) = viewModelScope.launch {
        messaging.streamMessages(_senderUid.value!! + _receiverUid.value!!)
            .removeObservers(lifecycleOwner)
        withContext(Dispatchers.IO) {
            messaging.closeMessagesStream()
        }
    }

    suspend fun sendFcm() = withContext(Dispatchers.IO) {
        val watchUID = children.getChildInformationByUID(_receiverUid.value!!).watchUID
        val watchToken = watches.getWatchToken(watchUID)
        Notification().sendMessageFCMToChildWatch(
            watchToken,
            receiverFirstName.value!!
        )
    }
}