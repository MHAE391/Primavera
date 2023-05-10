package com.m391.primavera.database.server

import android.content.Context
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.m391.primavera.utils.Constants.WATCHES
import kotlinx.coroutines.tasks.await

class WatchInformation(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val watches: CollectionReference = firestore.collection(WATCHES)
    suspend fun getWatchToken(id: String): String {
        val response = watches.document(id).get().await()
        return response.getString("token")!!
    }
}