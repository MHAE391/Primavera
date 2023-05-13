package com.m391.primavera.database.server

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestoreSettings
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.Constants.CHILD_AGE
import com.m391.primavera.utils.Constants.FATHER
import com.m391.primavera.utils.Constants.FATHERS
import com.m391.primavera.utils.Constants.FATHER_FIRST_NAME
import com.m391.primavera.utils.Constants.FATHER_LAST_NAME
import com.m391.primavera.utils.Constants.FATHER_PHONE
import com.m391.primavera.utils.Constants.IMAGE_PATH
import com.m391.primavera.utils.Constants.IMAGE_URI
import com.m391.primavera.utils.Constants.LATITUDE
import com.m391.primavera.utils.Constants.LONGITUDE
import com.m391.primavera.utils.Constants.NO
import com.m391.primavera.utils.Constants.RATE
import com.m391.primavera.utils.Constants.SUCCESS
import com.m391.primavera.utils.Constants.TEACHER
import com.m391.primavera.utils.Constants.TEACHERS
import com.m391.primavera.utils.Constants.TEACHERS_SUBJECTS
import com.m391.primavera.utils.Constants.TEACHER_ACADEMIC_YEARS
import com.m391.primavera.utils.Constants.TEACHER_UID
import com.m391.primavera.utils.Constants.YES
import com.m391.primavera.utils.models.ServerTeacherModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class TeacherInformation(
    private val context: Context,
    private val dataStoreManager: DataStoreManager
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val teachers: CollectionReference = firestore.collection(TEACHERS)
    private val mediaUploader = MediaUploader()
    private var registration: ListenerRegistration? = null
    private var currentUser: FirebaseUser?
    private val auth = Authentication()

    init {
        currentUser = auth.getCurrentUser()
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun getTeachers(): ArrayList<ServerTeacherModel> = withContext(Dispatchers.IO) {
        val teachersList = ArrayList<ServerTeacherModel>()
        val data = teachers.get().await()
        for (teacher in data) {
            teachersList.add(
                ServerTeacherModel(
                    teacherId = teacher.data[TEACHER_UID].toString(),
                    firstName = teacher.data[FATHER_FIRST_NAME].toString(),
                    lastName = teacher.data[FATHER_LAST_NAME].toString(),
                    longitude = teacher.data[LONGITUDE] as Number,
                    latitude = teacher.data[LATITUDE] as Number,
                    phone = teacher.data[FATHER_PHONE].toString(),
                    image = mediaUploader.imageDownloader(teacher.data[IMAGE_PATH].toString()),
                    imageUri = teacher.data[IMAGE_URI].toString(),
                    age = teacher.data[CHILD_AGE].toString(),
                    subjects = teacher.data[TEACHERS_SUBJECTS]!! as ArrayList<String>,
                    academicYears = teacher.data[TEACHER_ACADEMIC_YEARS]!! as ArrayList<String>,
                    rate = teacher.data[RATE] as Number
                )
            )
        }
        return@withContext teachersList
    }

    suspend fun streamTeachers(): LiveData<List<ServerTeacherModel>> = withContext(Dispatchers.IO) {
        val _teachersList = MutableLiveData<List<ServerTeacherModel>>()
        val teachersList: LiveData<List<ServerTeacherModel>> = _teachersList
        registration = teachers.addSnapshotListener { value, error ->
            if (error != null) {
                Timber.tag("Teachers Database").e(error, "Listen failed.")
                return@addSnapshotListener
            }
            val teacherList = ArrayList<ServerTeacherModel>()
            for (teacher in value!!) {
                teacherList.add(
                    ServerTeacherModel(
                        teacherId = teacher.data[TEACHER_UID].toString(),
                        firstName = teacher.data[FATHER_FIRST_NAME].toString(),
                        lastName = teacher.data[FATHER_LAST_NAME].toString(),
                        longitude = teacher.data[LONGITUDE] as Number,
                        latitude = teacher.data[LATITUDE] as Number,
                        phone = teacher.data[FATHER_PHONE].toString(),
                        image = teacher.data[IMAGE_PATH].toString(),
                        imageUri = teacher.data[IMAGE_URI].toString(),
                        age = teacher.data[CHILD_AGE].toString(),
                        subjects = teacher.data[TEACHERS_SUBJECTS]!! as ArrayList<String>,
                        academicYears = teacher.data[TEACHER_ACADEMIC_YEARS]!! as ArrayList<String>,
                        rate = teacher.data[RATE] as Number
                    )
                )
            }
            _teachersList.value = teacherList
        }
        return@withContext teachersList
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
        var response: String = SUCCESS
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
            FATHER_PHONE to currentUser?.phoneNumber,
            FATHER to NO,
            RATE to 5
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

    suspend fun closeUsersStream() = withContext(Dispatchers.IO) {
        if (registration != null) {
            registration!!.remove()
        }
    }

    suspend fun streamTeacherByUid(teacherUid: String): LiveData<ServerTeacherModel> =
        withContext(Dispatchers.IO) {
            val teacher = MutableLiveData<ServerTeacherModel>()
            registration = teachers.document(teacherUid).addSnapshotListener { value, error ->
                if (error != null) {
                    Timber.tag("Teachers Database").e(error, "Listen failed.")
                    return@addSnapshotListener
                } else {
                    val serverTeacher = ServerTeacherModel(
                        teacherId = value!!.data!![TEACHER_UID].toString(),
                        firstName = value.data!![FATHER_FIRST_NAME].toString(),
                        lastName = value.data!![FATHER_LAST_NAME].toString(),
                        longitude = value.data!![LONGITUDE] as Number,
                        latitude = value.data!![LATITUDE] as Number,
                        phone = value.data!![FATHER_PHONE].toString(),
                        image = value.data!![IMAGE_PATH].toString(),
                        imageUri = value.data!![IMAGE_URI].toString(),
                        age = value.data!![CHILD_AGE].toString(),
                        subjects = value.data!![TEACHERS_SUBJECTS]!! as ArrayList<String>,
                        academicYears = value.data!![TEACHER_ACADEMIC_YEARS]!! as ArrayList<String>,
                        rate = value.data!![RATE] as Number
                    )
                    teacher.value = serverTeacher
                }
            }
            return@withContext teacher
        }
}