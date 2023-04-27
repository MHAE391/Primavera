package com.m391.primavera.authentication.information.father

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.m391.primavera.utils.BaseViewModel

class FatherInformationViewModel(private val app: Application) : BaseViewModel(app) {
    val childWatch = MutableLiveData<String>()
    val fatherImage = MutableLiveData<String>()
    val childImage = MutableLiveData<String>()

    init {
        childWatch.value = "Select Your Child Watch"
    }

    fun getChildWatch(name: String) {
        childWatch.value = name
    }
}