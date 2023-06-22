package com.m391.primavera.notification

import com.google.gson.Gson
import com.m391.primavera.utils.Constants.MESSAGE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class Notification {
    fun sendAssignFCMToChildWatch(
        childName: String,
        childUID: String,
        watchToken: String,
        fatherName: String,
        fatherUID: String
    ) {
        val url = "https://fcm.googleapis.com/fcm/send"
        val apiKey =
            "AAAAPLfqcsQ:APA91bGkqZ5VzwBVbXQnkUzaFryV339sVFGQxrT_SB0Dl39ha5tmOdeBy5lS0S9Qg9T-IfOHd9xCCx-vGb8YvLSWehRehzi1O_ULwHwHye-kc8wlk0Wk44Oc2kqMf2_Wn8Z4F_s9oOO-"
        //  val deviceToken = "cYWwyCNNS3OiIui0gLyAxh:APA91bEK7qafPBQQMh4-fst6xoduZQI__59ybPVW0PemjqGvTeqz9925u5smv5ZG8_VnH1ZjALu6CVQzplVh8NGcU3K7UsJkl94DwvcNDh-hp9XmFLAq7kyFbHhn46MOivI-79YeYHmP"
        val payload = mapOf(
            "to" to watchToken,
            "data" to mapOf(
                "childName" to childName,
                "childUID" to childUID,
                "fatherUID" to fatherUID,
                "fatherName" to fatherName,
                "type" to "Assign"
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

    fun sendMessageFCMToChildWatch(watchToken: String, childName: String) {
        val url = "https://fcm.googleapis.com/fcm/send"
        val apiKey =
            "AAAAPLfqcsQ:APA91bGkqZ5VzwBVbXQnkUzaFryV339sVFGQxrT_SB0Dl39ha5tmOdeBy5lS0S9Qg9T-IfOHd9xCCx-vGb8YvLSWehRehzi1O_ULwHwHye-kc8wlk0Wk44Oc2kqMf2_Wn8Z4F_s9oOO-"
        //  val deviceToken = "cYWwyCNNS3OiIui0gLyAxh:APA91bEK7qafPBQQMh4-fst6xoduZQI__59ybPVW0PemjqGvTeqz9925u5smv5ZG8_VnH1ZjALu6CVQzplVh8NGcU3K7UsJkl94DwvcNDh-hp9XmFLAq7kyFbHhn46MOivI-79YeYHmP"
        val payload = mapOf(
            "to" to watchToken,
            "data" to mapOf(
                "childName" to childName,
                "type" to "Message"
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

    fun sendMessageFCMToFatherOrTeacher(
        receiverTopic: String,
        messageType: String,
        senderName: String,
        messageBody: String,
        senderId : String
    ) {
        val url = "https://fcm.googleapis.com/fcm/send"
        val apiKey =
            "AAAAPLfqcsQ:APA91bGkqZ5VzwBVbXQnkUzaFryV339sVFGQxrT_SB0Dl39ha5tmOdeBy5lS0S9Qg9T-IfOHd9xCCx-vGb8YvLSWehRehzi1O_ULwHwHye-kc8wlk0Wk44Oc2kqMf2_Wn8Z4F_s9oOO-"
        val topic = "/topics/${receiverTopic}"

        val payload = mapOf(
            "to" to topic,
            "data" to mapOf(
                "type" to MESSAGE,
                "senderName" to senderName,
                "messageBody" to messageBody,
                "senderId" to senderId
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