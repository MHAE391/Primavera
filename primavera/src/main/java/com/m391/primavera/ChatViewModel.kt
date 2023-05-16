package com.m391.primavera

import android.net.Uri
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.m391.primavera.DeviceIdGenerator.generateDeviceId
import com.m391.primavera.base.Constants
import com.m391.primavera.base.Constants.AUDIOS
import com.m391.primavera.base.Constants.CHATS
import com.m391.primavera.base.Constants.MEDIA_PATH
import com.m391.primavera.base.Constants.MESSAGE
import com.m391.primavera.base.Constants.MESSAGES
import com.m391.primavera.base.Constants.MESSAGE_TYPE
import com.m391.primavera.base.Constants.MESSAGE_UID
import com.m391.primavera.base.Constants.NO
import com.m391.primavera.base.Constants.SEEN
import com.m391.primavera.base.Constants.SENDER_UID
import com.m391.primavera.base.Constants.SUCCESS
import com.m391.primavera.base.Constants.TIME_SENT
import com.m391.primavera.base.Constants.VOICE_MESSAGE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class ChatViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<ServerMessageModel>>()
    val message: LiveData<List<ServerMessageModel>> = _messages
    private val messageArray = ArrayList<ServerMessageModel>()


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

    private var senderRoom: String? = null
    private lateinit var receiverRoom: String
    private lateinit var messageType: String
    private lateinit var senderUID: String
    private lateinit var receiverUID: String
    private lateinit var messageUID: String
    private lateinit var timeSent: Date


    init {
        viewModelScope.launch {
            FirebaseFirestore.getInstance().collection("Watches").document(generateDeviceId()).get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        receiverUID = it.result["fatherUID"].toString()
                        senderUID = it.result["childUID"].toString()
                        senderRoom = (senderUID + receiverUID)
                        receiverRoom = (receiverUID + senderUID)
                    }
                }.await()
            openStreamMessage()
        }
    }

    private var registration: ListenerRegistration? = null

    suspend fun openStreamMessage() = viewModelScope.launch {

        if (senderRoom != null) {
            registration =
                FirebaseFirestore.getInstance().collection(CHATS).document(senderRoom!!).collection(
                    MESSAGES
                ).addSnapshotListener { value, _ ->
                    for (message in value!!) {
                        val serverMessage = ServerMessageModel(
                            senderUID = message[SENDER_UID].toString(),
                            messageUID = message[MESSAGE_UID].toString(),
                            mediaPath = message[MEDIA_PATH].toString(),
                            messageType = message[MESSAGE_TYPE].toString(),
                            seen = message[SEEN].toString(),
                            timeSent = message[TIME_SENT, Date::class.java]!! as Date,
                            message = message[MESSAGE].toString()
                        )
                        if (!messageArray.contains(serverMessage) && serverMessage.senderUID != senderUID) {
                            messageArray.add(serverMessage)
                        }
                    }
                    _messages.postValue(messageArray)
                }
        }
    }

    suspend fun closeStreamMessage() = viewModelScope.launch {
        registration?.remove()
    }

    suspend fun sendAudioMessage(uri: String): String = withContext(Dispatchers.IO) {
        val audio = uploadAudio(uri)
        var response = SUCCESS
        timeSent = Calendar.getInstance().time
        messageUID = "${System.currentTimeMillis()}${UUID.randomUUID()}${senderUID}"
        messageType = VOICE_MESSAGE

        FirebaseFirestore.getInstance().collection(CHATS).document(senderRoom!!)
            .collection(MESSAGES)
            .document(messageUID).set(
                mapOf(
                    MEDIA_PATH to audio.audioPath,
                    MESSAGE to audio.audioUri,
                    MESSAGE_TYPE to messageType,
                    MESSAGE_UID to messageUID,
                    SEEN to NO,
                    SENDER_UID to senderUID,
                    TIME_SENT to timeSent
                )
            ).addOnFailureListener {
                response = it.localizedMessage!!
            }.await()

        FirebaseFirestore.getInstance().collection(CHATS).document(receiverRoom)
            .collection(MESSAGES).document(messageUID).set(
                mapOf(
                    MEDIA_PATH to audio.audioPath,
                    MESSAGE to audio.audioUri,
                    MESSAGE_TYPE to messageType,
                    MESSAGE_UID to messageUID,
                    SEEN to NO,
                    SENDER_UID to senderUID,
                    TIME_SENT to timeSent
                )
            ).addOnFailureListener {
                response = it.localizedMessage!!
            }.await()

        return@withContext response
    }

    private suspend fun uploadAudio(uri: String): ServerAudioModel = withContext(Dispatchers.IO) {
        val path = "${System.currentTimeMillis()}${UUID.randomUUID()}"
        val process = storageRef.getReference(AUDIOS).child(path).putFile(Uri.fromFile(File(uri)))
            .asDeferred().await()
        val audioUri = process.metadata!!.reference!!.downloadUrl.await()
        return@withContext ServerAudioModel(path, audioUri)
    }

    private val storageRef = FirebaseStorage.getInstance()
}