package com.m391.primavera.authentication.phone

import android.app.Activity
import android.app.Application
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
    fun sendOTPCode(activity: Activity?): String {
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
                serverDatabase.authentication.response.observeForever {
                    if (it == Constants.SUCCESS) {
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
                        println(it)
                        showSnackBar.value = it
                    }
                }

            }
        }
        return Constants.DONE
    }

    private fun isValidPhone(phone: String): Boolean {
        val expression = "^01[0-2]\\d{1,8}\$"
        val inputString: CharSequence = phone
        val pattern: Pattern = Pattern.compile(expression)
        val matcher: Matcher = pattern.matcher(inputString)
        return matcher.matches() and (phone.length == 11)
    }
}