package com.m391.primavera.database.server

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.Constants.CONVERSATIONS
import com.m391.primavera.utils.Constants.FATHERS
import com.m391.primavera.utils.Constants.FATHER_CONVERSATIONS
import com.m391.primavera.utils.Constants.FATHER_FIRST_NAME
import com.m391.primavera.utils.Constants.FATHER_LAST_NAME
import com.m391.primavera.utils.Constants.FATHER_PHONE
import com.m391.primavera.utils.Constants.TEACHERS
import com.m391.primavera.utils.Constants.TEACHER_CONVERSATIONS
import com.m391.primavera.utils.models.ServerConversationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class ConversationInformation(
    private val context: Context,
    private val dataStoreManager: DataStoreManager
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val  conversations: CollectionReference = firestore.collection(CONVERSATIONS)
    private var registration: ListenerRegistration? = null
    private val auth = Authentication()
    private var currentUser: FirebaseUser? = auth.getCurrentUser()
    private val teachers = TeacherInformation(context, dataStoreManager)
    private lateinit var receiverUID: String
    private lateinit var senderUID: String

    init {
        if (currentUser != null) senderUID = currentUser!!.uid
    }

    suspend fun createFatherWithTeacherConversation(receiver: String) =
        withContext(Dispatchers.IO) {
            receiverUID = receiver
            if (!checkIfConversationAlreadyExist()) {
                conversations.document(senderUID).collection(FATHER_CONVERSATIONS)
                    .document(receiverUID).set(
                        mapOf(
                            "receiverUid" to receiverUID
                        )
                    ).await()
                conversations.document(receiverUID).collection(TEACHER_CONVERSATIONS)
                    .document(senderUID)
                    .set(
                        mapOf(
                            "receiverUid" to senderUID
                        )
                    ).await()
            }
        }

    suspend fun createTeacherWithFatherConversation(receiver: String) =
        withContext(Dispatchers.IO) {
            receiverUID = receiver
            if (!checkIfConversationAlreadyExist()) {
                conversations.document(senderUID).collection(TEACHER_CONVERSATIONS)
                    .document(receiverUID).set(
                        mapOf(
                            "receiverUid" to receiverUID
                        )
                    ).await()
                conversations.document(receiverUID).collection(FATHER_CONVERSATIONS)
                    .document(senderUID)
                    .set(
                        mapOf(
                            "receiverUid" to senderUID
                        )
                    ).await()
            }
        }

    suspend fun streamFatherConversations(): LiveData<List<ServerConversationModel>> =
        withContext(Dispatchers.IO) {
            val conversationsList = MutableLiveData<List<ServerConversationModel>>()
            registration = conversations.document(senderUID).collection(FATHER_CONVERSATIONS)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Timber.tag("Conversations Database").e(error, "Listen failed.")
                        return@addSnapshotListener
                    }
                    val conversationsArray = ArrayList<ServerConversationModel>()
                    for (conversation in value!!) {
                        val receiver = conversation["receiverUid"].toString()
                        firestore.collection(TEACHERS).document(receiver).get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    conversationsArray.add(
                                        ServerConversationModel(
                                            receiverUID = receiver,
                                            firstName = task.result[FATHER_FIRST_NAME].toString(),
                                            lastName = task.result[FATHER_LAST_NAME].toString(),
                                            imageUrl = task.result[Constants.IMAGE_URI].toString(),
                                            phone = task.result[FATHER_PHONE].toString()
                                        )
                                    )
                                }
                                conversationsList.postValue(conversationsArray)
                            }
                        conversationsList.postValue(conversationsArray)
                    }
                }
            return@withContext conversationsList
        }


    suspend fun streamTeacherConversations(): LiveData<List<ServerConversationModel>> =
        withContext(Dispatchers.IO) {
            val conversationsList = MutableLiveData<List<ServerConversationModel>>()
            registration = conversations.document(senderUID).collection(TEACHER_CONVERSATIONS)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Timber.tag("Conversations Database").e(error, "Listen failed.")
                        return@addSnapshotListener
                    }
                    val conversationsArray = ArrayList<ServerConversationModel>()
                    for (conversation in value!!) {
                        val receiver = conversation["receiverUid"].toString()
                        firestore.collection(FATHERS).document(receiver).get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    conversationsArray.add(
                                        ServerConversationModel(
                                            receiverUID = receiver,
                                            firstName = task.result[FATHER_FIRST_NAME].toString(),
                                            lastName = task.result[FATHER_LAST_NAME].toString(),
                                            imageUrl = task.result[Constants.IMAGE_URI].toString(),
                                            phone = task.result[FATHER_PHONE].toString()
                                        )
                                    )
                                }
                                conversationsList.postValue(conversationsArray)
                            }
                        conversationsList.postValue(conversationsArray)
                    }
                }
            return@withContext conversationsList
        }

    private suspend fun checkIfConversationAlreadyExist(): Boolean = withContext(Dispatchers.IO) {
        if (currentUser != null) {
            senderUID = currentUser!!.uid
            val response =
                conversations.document(senderUID).collection(FATHER_CONVERSATIONS).document(receiverUID)
                    .get().await()
            return@withContext response.exists()
        }
        return@withContext false
    }

    suspend fun closeConversationsStream() = withContext(Dispatchers.IO) {
        if (registration != null) {
            registration!!.remove()
        }
    }
}