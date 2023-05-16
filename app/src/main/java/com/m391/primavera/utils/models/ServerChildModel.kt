package com.m391.primavera.utils.models

import com.google.mlkit.vision.common.InputImage

data class ServerChildModel(
    val childUID: String,
    val childName: String,
    val academicYear: String,
    val age: String,
    val image: Any,
    val imageUri: String,
    val watchUID: String,
    val fatherUID: String,
    var currentChild: String,
    val fatherName: String
)