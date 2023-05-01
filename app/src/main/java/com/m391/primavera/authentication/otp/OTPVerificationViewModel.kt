package com.m391.primavera.authentication.otp

import android.app.Activity
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.m391.primavera.database.datastore.DataStoreManager
import com.m391.primavera.database.server.ServerDatabase
import com.m391.primavera.utils.BaseViewModel
import com.m391.primavera.utils.Constants
import com.m391.primavera.utils.Constants.FATHER
import com.m391.primavera.utils.Constants.SUCCESSFUL_LOGIN
import kotlinx.coroutines.launch

class OTPVerificationViewModel(val app: Application) : BaseViewModel(app) {
    val firstCode = MutableLiveData<String?>()
    val secondCode = MutableLiveData<String?>()
    val thirdCode = MutableLiveData<String?>()
    val fourthCode = MutableLiveData<String?>()
    val fifthCode = MutableLiveData<String?>()
    val sixthCode = MutableLiveData<String?>()
    val phoneNumber = MutableLiveData<String?>()
    private val _response = MutableLiveData<String?>()
    val response: LiveData<String?> = _response
    private val resendToken = MutableLiveData<PhoneAuthProvider.ForceResendingToken>()
    private val storedVerificationId = MutableLiveData<String>()
    private val auth = ServerDatabase(app.applicationContext).authentication
    private val fathers = ServerDatabase(app.applicationContext).fatherInformation
    private val dataStoreManager = DataStoreManager(app.applicationContext)
    private val _alreadySigned = MutableLiveData<String?>()
    val alreadySigned: LiveData<String?> = _alreadySigned

    fun setupData(
        phone: String, token: PhoneAuthProvider.ForceResendingToken, verificationId: String
    ) {
        phoneNumber.value = phone
        resendToken.value = token
        storedVerificationId.value = verificationId
    }

    fun signInWithPhone() {
        if (validateOTP()) {
            showSnackBar.value = Constants.EMPTY_OTP
        } else {
            viewModelScope.launch {
                showLoading.value = true
                _response.value = null
                val codeOTP =
                    firstCode.value + secondCode.value + thirdCode.value + fourthCode.value + fifthCode.value + sixthCode.value
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    storedVerificationId.value!!, codeOTP
                )
                val login = auth.signInWithPhoneAuthCredential(credential)

                if (login == SUCCESSFUL_LOGIN) {
                    if (fathers.checkAlreadyFatherOrNot()) {
                        _alreadySigned.postValue(FATHER)
                        dataStoreManager.setUserUid(auth.getCurrentUser()!!.uid)
                    } else _alreadySigned.postValue("Not")
                }
                _response.postValue(login)
                showLoading.value = false
            }
        }

    }

    private fun validateOTP(): Boolean {
        return firstCode.value == null || secondCode.value == null || thirdCode.value == null || fourthCode.value == null || fifthCode.value == null || sixthCode.value == null
    }

    suspend fun resendOTPCode(activity: Activity) {
        viewModelScope.launch {
            showLoading.value = true
            auth.resendVerificationCode("+2${phoneNumber.value!!}", activity, resendToken.value!!)
            auth.response.observeForever {
                if (it == Constants.SUCCESS) {
                    showLoading.value = false
                    showToast.value = Constants.CODE_SENT
                    resendToken.value = auth.resendToken
                    storedVerificationId.value = auth.storedVerificationId
                } else if (it != null) {
                    showLoading.value = false
                    showSnackBar.value = it
                }
            }
        }
    }

    fun resetData() {
        firstCode.value = null
        secondCode.value = null
        thirdCode.value = null
        fourthCode.value = null
        fifthCode.value = null
        sixthCode.value = null
    }
}