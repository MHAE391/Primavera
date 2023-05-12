package com.m391.primavera.database.server

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.*
import com.m391.primavera.utils.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

import java.util.concurrent.TimeUnit


class Authentication {
    private val auth = FirebaseAuth.getInstance()
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    private val _response = MutableLiveData<String?>()
    val response: LiveData<String?> = _response
    var storedVerificationId: String? = null
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    suspend fun sendVerificationCodeAsync(
        phone: String, activity: Activity
    ) = withContext(Dispatchers.IO) {
        init()
        appCheck(activity)
        val options =
            PhoneAuthOptions.newBuilder(auth).setPhoneNumber(phone) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(activity)
                .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun resendVerificationCode(
        phone: String,
        activity: Activity,
        token: PhoneAuthProvider.ForceResendingToken
    ) = withContext(Dispatchers.IO) {
        init()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity)                 // Activity (for callback binding)
            .setCallbacks(callbacks)
            .setForceResendingToken(token)// OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.

        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            // Show a message and update the UI
            _response.value = e.message
        }

        override fun onCodeSent(
            verificationId: String, token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.

            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token
            _response.value = Constants.SUCCESS
        }
    }

    suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): String? =
        withContext(Dispatchers.IO) {
            var authResponse: String? = null
            init()
            try {
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            authResponse = Constants.SUCCESSFUL_LOGIN
                        } else {
                            if (task.exception is FirebaseAuthInvalidCredentialsException) {
                                authResponse = Constants.INVALID_CODE
                            }
                        }
                    }.await()

            } catch (e: Exception) {
                authResponse = Constants.INVALID_CODE
            }
            return@withContext authResponse
        }

    suspend fun init() = withContext(Dispatchers.IO) {
        if (response.value != null) _response.postValue(null)
    }

    private fun appCheck(activity: Activity) {
        FirebaseApp.initializeApp(activity)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
    }

    suspend fun logOut() {
        auth.signOut()
    }

}
