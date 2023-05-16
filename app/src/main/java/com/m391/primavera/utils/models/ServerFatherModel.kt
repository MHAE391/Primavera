package com.m391.primavera.utils.models

data class ServerFatherModel(
    val fatherUID: String,
    val children: List<String>,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val imageUri: String,
    val image: Any,
    val latitude: Number,
    val longitude: Number,
)
