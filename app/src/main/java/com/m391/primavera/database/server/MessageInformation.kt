package com.m391.primavera.database.server

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.utils.Constants.CHATS
import com.m391.primavera.utils.Constants.IMAGE_MESSAGE
import com.m391.primavera.utils.Constants.MEDIA_PATH
import com.m391.primavera.utils.Constants.MESSAGE
import com.m391.primavera.utils.Constants.MESSAGES
import com.m391.primavera.utils.Constants.MESSAGE_TYPE
import com.m391.primavera.utils.Constants.MESSAGE_UID
import com.m391.primavera.utils.Constants.NO
import com.m391.primavera.utils.Constants.SEEN
import com.m391.primavera.utils.Constants.SENDER_UID
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.Constants.TEXT_MESSAGE
import com.m391.primavera.utils.Constants.TIME_SENT
import com.m391.primavera.utils.Constants.VOICE_MESSAGE
import com.m391.primavera.utils.models.ServerMessageModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class MessageInformation(
    private val context: Context, private val dataStoreManager: DataStoreManager
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val chats = firestore.collection(CHATS)
    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String
    private lateinit var messageType: String
    private lateinit var senderUID: String
    private lateinit var receiverUID: String
    private lateinit var messageContent: String
    private lateinit var messageUID: String
    private lateinit var timeSent: Date
    private var registration: ListenerRegistration? = null
    private val mediaUploader = MediaUploader()
    val auth = Authentication()
    private val fcm = FirebaseMessaging.getInstance()
    suspend fun sendMessage(
        messageType: String,
        senderUID: String,
        receiverUID: String,
        messageContent: String
    ): String =
        withContext(Dispatchers.IO) {
            this@MessageInformation.senderRoom = (senderUID + receiverUID)
            this@MessageInformation.receiverRoom = (receiverUID + senderUID)
            this@MessageInformation.messageUID =
                "${System.currentTimeMillis()}${UUID.randomUUID()}${senderUID}"
            this@MessageInformation.timeSent = Calendar.getInstance().time
            this@MessageInformation.messageType = messageType
            this@MessageInformation.senderUID = (senderUID)
            this@MessageInformation.receiverUID = (receiverUID)
            this@MessageInformation.messageContent = (messageContent)

            val response = when (messageType) {
                TEXT_MESSAGE -> sendTextMessage()
                VOICE_MESSAGE -> sendVoiceMessage()
                else -> sendImageMessage()
            }
            return@withContext response
        }

    private suspend fun sendTextMessage(): String = withContext(Dispatchers.IO) {
        val message = mapOf(
            MESSAGE_UID to messageUID,
            SENDER_UID to senderUID,
            TIME_SENT to timeSent,
            MESSAGE to messageContent,
            SEEN to NO,
            MESSAGE_TYPE to TEXT_MESSAGE
        )
        return@withContext uploadMessage(message)
    }

    private suspend fun sendVoiceMessage() = withContext(Dispatchers.IO) {
        val audio = mediaUploader.uploadAudio(messageContent)
        val message = mapOf(
            MESSAGE_UID to messageUID,
            SENDER_UID to senderUID,
            TIME_SENT to timeSent,
            MESSAGE to audio.audioUri,
            SEEN to NO,
            MESSAGE_TYPE to VOICE_MESSAGE,
            MEDIA_PATH to audio.audioPath
        )
        return@withContext uploadMessage(message)
    }

    private suspend fun sendImageMessage(): String = withContext(Dispatchers.IO) {
        val image = mediaUploader.uploadImage(messageContent)
        val message = mapOf(
            MESSAGE_UID to messageUID,
            SENDER_UID to senderUID,
            TIME_SENT to timeSent,
            MESSAGE to image.imageUri,
            SEEN to NO,
            MESSAGE_TYPE to IMAGE_MESSAGE,
            MEDIA_PATH to image.imagePath
        )
        return@withContext uploadMessage(message)
    }

    private suspend fun uploadMessage(message: Map<String, Any>): String =
        withContext(Dispatchers.IO) {
            var response: String = SUCCESS
            chats.document(senderRoom).collection(MESSAGES).document(messageUID)
                .set(message).addOnFailureListener {
                    response = it.message!!
                }.await()
            chats.document(receiverRoom).collection(MESSAGES).document(messageUID)
                .set(message).addOnFailureListener {
                    response = it.message!!
                }.await()
            return@withContext response
        }

    suspend fun streamMessages(room: String): LiveData<List<ServerMessageModel>> =
        withContext(Dispatchers.IO) {
            val messages = MutableLiveData<List<ServerMessageModel>>()
            registration =
                chats.document(room).collection(MESSAGES).addSnapshotListener { value, error ->
                    if (error != null) {
                        Timber.tag("Messages Database").e(error, "Listen failed.")
                        return@addSnapshotListener
                    }
                    val messagesList = ArrayList<ServerMessageModel>()
                    for (message in value!!) {
                        messagesList.add(
                            ServerMessageModel(
                                senderUID = message[SENDER_UID].toString(),
                                messageUID = message[MESSAGE_UID].toString(),
                                mediaPath = message[MEDIA_PATH].toString(),
                                messageType = message[MESSAGE_TYPE].toString(),
                                seen = message[SEEN].toString(),
                                timeSent = message[TIME_SENT, Date::class.java]!! as Date,
                                message = message[MESSAGE].toString()
                            )
                        )
                    }
                    messages.postValue(messagesList)
                }
            return@withContext messages
        }

    suspend fun closeMessagesStream() = withContext(Dispatchers.IO) {
        if (registration != null) {
            registration!!.remove()
        }
    }

    suspend fun subscribeToTopic() = withContext(Dispatchers.IO) {
        fcm.subscribeToTopic("user${auth.getCurrentUser()!!.uid}").await()
    }
    suspend fun sendFcm() = withContext(Dispatchers.IO){

    }

}