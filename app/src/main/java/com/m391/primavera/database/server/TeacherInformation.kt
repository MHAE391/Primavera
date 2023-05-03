package com.m391.primavera.database.server

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.Constants.CHILD_AGE
import com.m391.primavera.utils.Constants.FATHER
import com.m391.primavera.utils.Constants.FATHERS
import com.m391.primavera.utils.Constants.FATHER_FIRST_NAME
import com.m391.primavera.utils.Constants.FATHER_LAST_NAME
import com.m391.primavera.utils.Constants.IMAGE_PATH
import com.m391.primavera.utils.Constants.IMAGE_URI
import com.m391.primavera.utils.Constants.LATITUDE
import com.m391.primavera.utils.Constants.LONGITUDE
import com.m391.primavera.utils.Constants.NO
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.Constants.TEACHER
import com.m391.primavera.utils.Constants.TEACHERS
import com.m391.primavera.utils.Constants.TEACHERS_SUBJECTS
import com.m391.primavera.utils.Constants.TEACHER_ACADEMIC_YEARS
import com.m391.primavera.utils.Constants.TEACHER_UID
import com.m391.primavera.utils.Constants.YES
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TeacherInformation(
    private val context: Context,
    private val dataStoreManager: DataStoreManager
) {
    private val firestore = FirebaseFirestore.getInstance()
    private var teachers: CollectionReference
    private val mediaUploader = MediaUploader()
    private var currentUser: FirebaseUser?
    private val auth = Authentication()

    init {
        val settings = firestoreSettings {
            isPersistenceEnabled = false
        }
        firestore.firestoreSettings = settings
        teachers = firestore.collection(TEACHERS)
        currentUser = auth.getCurrentUser()
    }


    suspend fun uploadTeacher(
        teacherFirstName: String,
        teacherLastName: String,
        teacherAge: String,
        teacherImage: String,
        teacherAcademicYears: List<String>,
        subjects: List<String>,
        latitude: Number,
        longitude: Number
    ): String = withContext(Dispatchers.IO) {
        var response: String = Constants.SUCCESS
        val teacherUid = currentUser?.uid
        val image = mediaUploader.uploadImage(teacherImage)
        val teacher = hashMapOf(
            TEACHER_UID to teacherUid,
            FATHER_FIRST_NAME to teacherFirstName,
            FATHER_LAST_NAME to teacherLastName,
            IMAGE_PATH to image.imagePath,
            IMAGE_URI to image.imageUri,
            CHILD_AGE to teacherAge,
            TEACHER_ACADEMIC_YEARS to teacherAcademicYears,
            TEACHERS_SUBJECTS to subjects,
            LONGITUDE to longitude,
            LATITUDE to latitude,
            FATHER to NO
        )
        teachers.document(teacherUid!!).set(teacher).addOnFailureListener {
            response = it.message!!
        }.await()
        if (response == SUCCESS) {
            dataStoreManager.setUserUid(teacherUid)
            dataStoreManager.setUserType(TEACHER)
        }
        return@withContext response
    }

    suspend fun checkAlreadyTeacherOrNot(): Boolean = withContext(Dispatchers.IO) {
        currentUser = auth.getCurrentUser()
        if (currentUser != null) {
            val teacherUid = currentUser!!.uid
            val response = teachers.document(teacherUid).get().await()
            return@withContext response.exists()
        }
        return@withContext false
    }
}