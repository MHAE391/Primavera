package com.m391.primavera.database.server

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.notification.Notification
import com.m391.primavera.utils.Constants.CHILDREN
import com.m391.primavera.utils.Constants.CHILD_ACADEMIC_YEAR
import com.m391.primavera.utils.Constants.CHILD_AGE
import com.m391.primavera.utils.Constants.CHILD_NAME
import com.m391.primavera.utils.Constants.CHILD_UID
import com.m391.primavera.utils.Constants.CHILD_WATCH_UID
import com.m391.primavera.utils.Constants.DATE_OF_BARTH
import com.m391.primavera.utils.Constants.ERROR
import com.m391.primavera.utils.Constants.FATHER_NAME
import com.m391.primavera.utils.Constants.FATHER_PHONE
import com.m391.primavera.utils.Constants.FATHER_UID
import com.m391.primavera.utils.Constants.IMAGE_PATH
import com.m391.primavera.utils.Constants.IMAGE_URI
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.models.ServerChildModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class ChildInformation(
    private val context: Context, private val dataStoreManager: DataStoreManager

) {
    private val firestore = FirebaseFirestore.getInstance()
    private val mediaUploader = MediaUploader()
    private var currentUser: FirebaseUser?
    private val children: CollectionReference = firestore.collection(CHILDREN)
    private val auth = Authentication()
    private var registration: ListenerRegistration? = null
    private val watchInformation = WatchInformation(context)
    private val notification = Notification()

    init {
        currentUser = auth.getCurrentUser()
    }

    suspend fun uploadChild(
        name: String,
        dateOfBarth: String,
        watchUid: String,
        image: String,
        academicYear: String,
        fatherName: String
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
            DATE_OF_BARTH to dateOfBarth,
            CHILD_ACADEMIC_YEAR to academicYear,
            FATHER_NAME to fatherName
        )
        children.document(childUid).set(child).addOnFailureListener {
            response = ERROR
        }.await()
        if (response != SUCCESS) return@withContext response
        dataStoreManager.setCurrentChildUid(childUid)
        dataStoreManager.setWatchUid(watchUid)
        return@withContext childUid
    }

    suspend fun streamChildInformationByUID(UID: String): LiveData<ServerChildModel> =
        withContext(Dispatchers.IO) {
            val child = MutableLiveData<ServerChildModel>()
            registration = children.document(UID).addSnapshotListener { value, error ->
                if (error != null) {
                    Timber.tag("Fathers Database").e(error, "Listen failed.")
                    return@addSnapshotListener
                }
                child.postValue(
                    ServerChildModel(
                        fatherUID = value!![FATHER_UID].toString(),
                        childUID = value[CHILD_UID].toString(),
                        childName = value[CHILD_NAME].toString(),
                        academicYear = value[CHILD_ACADEMIC_YEAR].toString(),
                        dateOfBarth = value[DATE_OF_BARTH].toString(),
                        image = value[IMAGE_PATH].toString(),
                        imageUri = value[IMAGE_URI].toString(),
                        watchUID = value[CHILD_WATCH_UID].toString(),
                        currentChild = "",
                        fatherName = value[FATHER_NAME].toString()
                    )
                )
            }
            return@withContext child
        }

    suspend fun streamChildren(): LiveData<List<ServerChildModel>> = withContext(Dispatchers.IO) {
        val childrenList = MutableLiveData<List<ServerChildModel>>()
        registration = children.addSnapshotListener { value, error ->
            if (error != null) {
                Timber.tag("Fathers Database").e(error, "Listen failed.")
                return@addSnapshotListener
            }
            val childrenArray = ArrayList<ServerChildModel>()
            for (child in value!!) {
                childrenArray.add(
                    ServerChildModel(
                        fatherUID = child[FATHER_UID].toString(),
                        childUID = child[CHILD_UID].toString(),
                        childName = child[CHILD_NAME].toString(),
                        academicYear = child[CHILD_ACADEMIC_YEAR].toString(),
                        dateOfBarth = child[DATE_OF_BARTH].toString(),
                        image = child[IMAGE_PATH].toString(),
                        imageUri = child[IMAGE_URI].toString(),
                        watchUID = child[CHILD_WATCH_UID].toString(),
                        currentChild = "",
                        fatherName = child[FATHER_NAME].toString()
                    )
                )
            }
            childrenList.value = childrenArray
        }
        return@withContext childrenList
    }

    suspend fun getChildInformationByUID(UID: String): ServerChildModel =
        withContext(Dispatchers.IO) {
            val result = children.document(UID).get().await()
            return@withContext ServerChildModel(
                fatherUID = result[FATHER_UID].toString(),
                childUID = result[CHILD_UID].toString(),
                childName = result[CHILD_NAME].toString(),
                academicYear = result[CHILD_ACADEMIC_YEAR].toString(),
                dateOfBarth = result[DATE_OF_BARTH].toString(),
                image = result[IMAGE_PATH].toString(),
                imageUri = result[IMAGE_URI].toString(),
                watchUID = result[CHILD_WATCH_UID].toString(),
                currentChild = "",
                fatherName = result[FATHER_NAME].toString()
            )
        }

    suspend fun closeChildStream() = withContext(Dispatchers.IO) {
        if (registration != null) {
            registration!!.remove()
        }
    }

    suspend fun addNewChild(
        name: String,
        dateOfBarth: String,
        watchUid: String,
        image: String,
        academicYear: String,
        fatherName: String
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
            DATE_OF_BARTH to dateOfBarth,
            CHILD_ACADEMIC_YEAR to academicYear,
            FATHER_NAME to fatherName
        )
        children.document(childUid).set(child).addOnFailureListener {
            response = ERROR
        }.await()
        if (response != SUCCESS) return@withContext response
        val token = watchInformation.getWatchToken(watchUid)
        notification.sendAssignFCMToChildWatch(
            childName = name,
            childUID = childUid,
            watchToken = token,
            fatherName = fatherName,
            fatherUID = fatherUid!!
        )
        return@withContext childUid
    }

    suspend fun updateChildInformation(
        watchUid: String,
        image: String,
        academicYear: String,
        childUid: String,
        fatherName: String,
        fatherUid: String,
        childName: String
    ): String = withContext(Dispatchers.IO) {
        var response = SUCCESS
        if (image != "No New Image") response = updateChildImage(childUid, image)
        if (academicYear != "No New Academic Year") response =
            updateChildAcademicYear(childUid, academicYear)
        if (watchUid != "No New Watch") response =
            updateChildWatch(childUid, watchUid, fatherName, fatherUid, childName)
        return@withContext response
    }

    private suspend fun updateChildImage(childUid: String, image: String): String =
        withContext(Dispatchers.IO) {
            var response = SUCCESS
            val childImage = mediaUploader.uploadImage(image)
            val uploadImage = hashMapOf(
                IMAGE_PATH to childImage.imagePath,
                IMAGE_URI to childImage.imageUri
            )
            children.document(childUid).update(
                uploadImage as Map<String, Any>
            ).addOnFailureListener {
                response = ERROR
            }.await()
            return@withContext response
        }

    private suspend fun updateChildWatch(
        childUid: String,
        watchUid: String,
        fatherName: String,
        fatherUid: String,
        childName: String
    ): String =
        withContext(Dispatchers.IO) {
            var response = SUCCESS
            children.document(childUid).update(
                CHILD_WATCH_UID, watchUid
            ).addOnFailureListener {
                response = ERROR
            }.await()
            if (response != SUCCESS) return@withContext response
            val token = watchInformation.getWatchToken(watchUid)
            notification.sendAssignFCMToChildWatch(
                childName = childName,
                childUID = childUid,
                watchToken = token,
                fatherName = fatherName,
                fatherUID = fatherUid
            )
            return@withContext response
        }

    private suspend fun updateChildAcademicYear(childUid: String, academicYear: String): String =
        withContext(Dispatchers.IO) {
            var response = SUCCESS
            children.document(childUid).update(
                CHILD_ACADEMIC_YEAR, academicYear
            ).addOnFailureListener {
                response = ERROR
            }.await()
            return@withContext response
        }

    suspend fun deleteChild(childUid: String): String = withContext(Dispatchers.IO) {
        var response = SUCCESS
        children.document(childUid).delete().addOnFailureListener {
            response = it.localizedMessage!!
        }.await()
        return@withContext response
    }
}