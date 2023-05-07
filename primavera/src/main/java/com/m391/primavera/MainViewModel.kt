package com.m391.primavera

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@SuppressLint("HardwareIds")
class MainViewModel(private val applicationContext: Application) : ViewModel() {
    val id = MutableLiveData<String>()

    init {
        viewModelScope.launch {
            id.value = Settings.Secure.getString(
                applicationContext.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        }
    }
}