package com.m391.primavera.database.server

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.utils.Constants.CHILDREN
import com.m391.primavera.utils.Constants.CHILD_ACADEMIC_YEAR
import com.m391.primavera.utils.Constants.CHILD_AGE
import com.m391.primavera.utils.Constants.CHILD_NAME
import com.m391.primavera.utils.Constants.CHILD_UID
import com.m391.primavera.utils.Constants.CHILD_WATCH_UID
import com.m391.primavera.utils.Constants.ERROR
import com.m391.primavera.utils.Constants.FATHER_PHONE
import com.m391.primavera.utils.Constants.FATHER_UID
import com.m391.primavera.utils.Constants.IMAGE_PATH
import com.m391.primavera.utils.Constants.IMAGE_URI
import com.m391.primavera.utils.Constants.SUCCESS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChildInformation(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val mediaUploader = MediaUploader()
    private var currentUser: FirebaseUser?
    private val dataStoreManager = DataStoreManager(context)
    private var children: CollectionReference
    private val auth = Authentication()

    init {
        val settings = firestoreSettings {
            isPersistenceEnabled = false
        }
        firestore.firestoreSettings = settings
        children = firestore.collection(CHILDREN)
        currentUser = auth.getCurrentUser()
    }

    suspend fun uploadChild(
        name: String, age: String, watchUid: String, image: String, academicYear: String
    ): String = withContext(Dispatchers.IO) {
        var response: String = SUCCESS
        val childUid = firestore.collection(CHILDREN).document().id
        val fatherUid = currentUser?.uid
        val childImage = mediaUploader.uploadImage(image)
        val fatherPhone = currentUser?.phoneNumber
        val child = hashMapOf(
            CHILD_UID to childUid,
            FATHER_UID to fatherUid,
            IMAGE_PATH to childImage.imagePath,
            IMAGE_URI to childImage.imageUri,
            FATHER_PHONE to fatherPhone,
            CHILD_NAME to name,
            CHILD_WATCH_UID to watchUid,
            CHILD_AGE to age,
            CHILD_ACADEMIC_YEAR to academicYear
        )
        children.document(childUid).set(child).addOnFailureListener {
            response = ERROR
        }.await()
        if (response != SUCCESS) return@withContext response
        dataStoreManager.setCurrentChildUid(childUid)
        return@withContext childUid
    }

}