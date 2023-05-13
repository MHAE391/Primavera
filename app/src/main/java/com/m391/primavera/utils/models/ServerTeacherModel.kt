package com.m391.primavera.utils.models

data class ServerTeacherModel(
    val teacherId: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val image: Any,
    val imageUri: String,
    val latitude: Number,
    val longitude: Number,
    val age: String,
    val academicYears: ArrayList<String>,
    val subjects: ArrayList<String>,
    val rate: Number

)
