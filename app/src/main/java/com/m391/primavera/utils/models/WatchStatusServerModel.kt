package com.m391.primavera.utils.models

data class WatchStatusServerModel(
    val heartRate: Number,
    val dailySteps: Number,
    val oxygenLevel: Number,
    val longitude: Double,
    val latitude: Double
)