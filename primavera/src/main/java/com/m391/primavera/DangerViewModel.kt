package com.m391.primavera

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.m391.primavera.base.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class DangerViewModel(app: Application) : AndroidViewModel(app) {

    val fatherUid = MutableLiveData<String>()
    private val childUid = MutableLiveData<String>()
    private val childName = MutableLiveData<String>()
    private val fatherName = MutableLiveData<String>()
    private val dataStoreManager = DataStoreManager.getInstance(app.applicationContext)


    init {
        viewModelScope.launch {
            setUpData()
        }
    }

    fun setUpData() {
        viewModelScope.launch {
            fatherUid.value = dataStoreManager.getFatherUid()
            fatherName.value = dataStoreManager.getFatherName()
            childName.value = dataStoreManager.getCurrentChildName()
            childUid.value = dataStoreManager.getCurrentChildUid()
        }
    }

    suspend fun dangerMode(lifecycleOwner: LifecycleOwner, longitude: Double, latitude: Double) =
        withContext(Dispatchers.Main) {
            fatherUid.observe(lifecycleOwner) { uid ->
                if (uid != null) {
                    val url = "https://fcm.googleapis.com/fcm/send"
                    val apiKey =
                        "AAAAXjFA5lY:APA91bGoimiISHOq8fG1JNouYeb48U0GXpSsg__YJ5W5-cjDQ_k6dL6UGol9lVWe1ijQXE7xBT34X8BZJvzcj1L6VFZ-4xAHzVEytiAdlt9CCo_yvFQHhwfgd9NDQeTiLushGrQ-mt5_"
                    val topic = "/topics/user${fatherUid.value}"

                    val payload = mapOf(
                        "to" to topic,
                        "data" to mapOf(
                            "type" to Constants.DANGER,
                            "childName" to childName.value,
                            "longitude" to longitude.toString(),
                            "latitude" to latitude.toString(),
                            "childUid" to childUid.value
                        )
                    )
                    runBlocking(Dispatchers.IO) {
                        launch {
                            val json = Gson().toJson(payload)
                            val requestBody =
                                json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                            val request = Request.Builder().url(url).post(requestBody)
                                .addHeader("Authorization", "key=$apiKey").build()

                            val client = OkHttpClient()
                            client.newCall(request).enqueue(object : Callback {
                                override fun onResponse(call: Call, response: Response) {
                                    // Handle successful response here
                                    println("done")
                                }

                                override fun onFailure(call: Call, e: IOException) {
                                    // Handle error here
                                    println("failed")
                                }
                            })
                        }
                    }
                }
            }
        }

}