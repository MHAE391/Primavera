package com.m391.primavera.authentication.phone

import android.app.Activity
import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.NavigationCommand
import kotlinx.coroutines.launch
import java.util.regex.Matcher
import java.util.regex.Pattern

class PhoneNumberViewModel(val app: Application) : BaseViewModel(app) {
    val phoneNumber = MutableLiveData<String>()
    private val dataStoreManager = DataStoreManager.getInstance(app.applicationContext)
    private val serverDatabase = ServerDatabase(app.applicationContext, dataStoreManager)
    fun sendOTPCode(activity: Activity?, lifecycleOwner: LifecycleOwner): String {
        if (phoneNumber.value.isNullOrBlank()) {
            showSnackBar.value = "Please Enter Yor Phone"
        } else if (!isValidPhone(phoneNumber.value!!.trim())) {
            showSnackBar.value = "Invalid Number"
        } else {
            viewModelScope.launch {
                showLoading.value = true
                serverDatabase.authentication.sendVerificationCodeAsync(
                    "+2${phoneNumber.value}", activity!!
                )
                if (serverDatabase.authentication.response.hasActiveObservers()) serverDatabase.authentication.response.removeObservers(
                    lifecycleOwner
                )
                serverDatabase.authentication.response.observe(lifecycleOwner) {
                    if (it == Constants.SUCCESS && serverDatabase.authentication.storedVerificationId != null && serverDatabase.authentication.resendToken != null) {
                        showLoading.value = false
                        showToast.value = Constants.CODE_SENT
                        navigationCommand.value = NavigationCommand.To(
                            PhoneNumberFragmentDirections.actionPhoneNumberFragmentToOTPVerificationFragment(
                                phoneNumber.value!!,
                                serverDatabase.authentication.storedVerificationId!!,
                                serverDatabase.authentication.resendToken!!
                            )
                        )
                    } else if (it != null) {
                        showLoading.value = false
                        showSnackBar.value = it
                    }
                }

            }
        }
        return Constants.DONE
    }

    suspend fun closeObserver(lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            if (serverDatabase.authentication.response.hasActiveObservers()) serverDatabase.authentication.response.removeObservers(
                lifecycleOwner
            )
        }
    }

    private fun isValidPhone(phone: String): Boolean {
        val expression = "^01[0-2]\\d{1,8}\$"
        val inputString: CharSequence = phone
        val pattern: Pattern = Pattern.compile(expression)
        val matcher: Matcher = pattern.matcher(inputString)
        return matcher.matches() and (phone.length == 11)
    }
}