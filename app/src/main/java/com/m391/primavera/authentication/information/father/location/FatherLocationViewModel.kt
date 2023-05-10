package com.m391.primavera.authentication.information.father.location

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.m391.primavera.utils.BaseViewModel

class FatherLocationViewModel(app: Application) : BaseViewModel(app) {
    val selectedLatitude = MutableLiveData<Number?>()
    val selectedLongitude = MutableLiveData<Number?>()

    fun setLocation(longitude: Number, latitude: Number) {
        selectedLatitude.value = latitude
        selectedLongitude.value = longitude
    }
}