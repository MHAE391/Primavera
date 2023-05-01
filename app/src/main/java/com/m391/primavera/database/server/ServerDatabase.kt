package com.m391.primavera.database.server

import android.content.Context

class
ServerDatabase(private val context: Context) {
    val authentication = Authentication()
    val fatherInformation = FatherInformation(context)
    val childInformation = ChildInformation(context)
}