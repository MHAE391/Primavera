package com.m391.primavera.database.server

import android.content.Context
import com.m391.primavera.database.datastore.DataStoreManager

class
ServerDatabase(private val context: Context) {
    val authentication = Authentication()
    private val dataStoreManager by lazy { DataStoreManager(context) }
    val fatherInformation = FatherInformation(context, dataStoreManager)
    val childInformation = ChildInformation(context, dataStoreManager)
    val teacherInformation = TeacherInformation(context, dataStoreManager)
}