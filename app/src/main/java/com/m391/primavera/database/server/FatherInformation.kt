package com.m391.primavera.database.server

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.notification.Notification
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.Constants.CHILDREN
import com.m391.primavera.utils.Constants.ERROR
import com.m391.primavera.utils.Constants.FATHER
import com.m391.primavera.utils.Constants.FATHERS
import com.m391.primavera.utils.Constants.FATHER_FIRST_NAME
import com.m391.primavera.utils.Constants.FATHER_LAST_NAME
import com.m391.primavera.utils.Constants.FATHER_PHONE
import com.m391.primavera.utils.Constants.FATHER_UID
import com.m391.primavera.utils.Constants.IMAGE_PATH
import com.m391.primavera.utils.Constants.IMAGE_URI
import com.m391.primavera.utils.Constants.LATITUDE
import com.m391.primavera.utils.Constants.LONGITUDE
import com.m391.primavera.utils.Constants.NO
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.Constants.TEACHER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FatherInformation(
    private val context: Context,
    private val dataStoreManager: DataStoreManager
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val fathers: CollectionReference = firestore.collection(FATHERS)
    private val mediaUploader = MediaUploader()
    private val childInformation = ChildInformation(context, dataStoreManager)
    private val watchInformation = WatchInformation(context)
    private val notification = Notification()
    private var currentUser: FirebaseUser?
    private val auth = Authentication()

    init {
        currentUser = auth.getCurrentUser()
    }

    suspend fun uploadFather(
        fatherFirstName: String,
        fatherLastName: String,
        childName: String,
        childAge: String,
        watchUid: String,
        fatherImage: String,
        childAcademicYear: String,
        childImage: String,
        latitude: Number,
        longitude: Number
    ): String = withContext(Dispatchers.IO) {
        var response: String = SUCCESS
        val fatherUid = currentUser?.uid
        val childUid = childInformation.uploadChild(
            name = childName,
            age = childAge,
            watchUid = watchUid,
            image = childImage,
            academicYear = childAcademicYear
        )
        if (childUid == ERROR) return@withContext ERROR
        val image = mediaUploader.uploadImage(fatherImage)
        val father = hashMapOf(
            FATHER_UID to fatherUid,
            FATHER_FIRST_NAME to fatherFirstName,
            FATHER_LAST_NAME to fatherLastName,
            IMAGE_PATH to image.imagePath,
            IMAGE_URI to image.imageUri,
            CHILDREN to listOf(childUid),
            TEACHER to NO,
            LONGITUDE to longitude,
            LATITUDE to latitude,
            FATHER_PHONE to currentUser?.phoneNumber
        )
        fathers.document(fatherUid!!).set(father).addOnFailureListener {
            response = it.message!!
        }.await()
        if (response == SUCCESS) {
            dataStoreManager.setUserUid(fatherUid)
            dataStoreManager.setUserType(FATHER)
            val token = watchInformation.getWatchToken(watchUid)
            notification.sendAssignFCMToChildWatch(
                childName = childName,
                childUID = childUid,
                watchToken = token,
                fatherName = fatherFirstName,
                fatherUID = fatherUid
            )
        }
        return@withContext response
    }

    suspend fun checkAlreadyFatherOrNot(): Boolean = withContext(Dispatchers.IO) {
        currentUser = auth.getCurrentUser()
        if (currentUser != null) {
            val fatherUid = currentUser!!.uid
            val response = fathers.document(fatherUid).get().await()
            return@withContext response.exists()
        }
        return@withContext false
    }

}