package com.m391.primavera.database.server

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.google.firebase.storage.FirebaseStorage
import com.m391.primavera.utils.Constants.AUDIOS
import com.m391.primavera.utils.Constants.IMAGES
import com.m391.primavera.utils.Constants.ONE_MEGABYTE
import com.m391.primavera.utils.models.ServerAudioModel
import com.m391.primavera.utils.models.ServerImageModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class MediaUploader {
    private val storageRef = FirebaseStorage.getInstance()
    suspend fun uploadImage(uri: String): ServerImageModel = withContext(Dispatchers.IO) {
        val path = "${System.currentTimeMillis()}${UUID.randomUUID()}"
        val process = storageRef.getReference(IMAGES)
            .child(path)
            .putFile(uri.toUri()).asDeferred().await()
        val imageUri = process.metadata!!.reference!!.downloadUrl.await()
        return@withContext ServerImageModel(path, imageUri)
    }

    suspend fun uploadAudio(uri: String): ServerAudioModel = withContext(Dispatchers.IO) {
        val path = "${System.currentTimeMillis()}${UUID.randomUUID()}"
        val process = storageRef.getReference(AUDIOS)
            .child(path)
            .putFile(Uri.fromFile(File(uri))).asDeferred().await()
        val audioUri = process.metadata!!.reference!!.downloadUrl.await()
        return@withContext ServerAudioModel(path, audioUri)
    }

    suspend fun imageDownloader(src: String): ByteArray = withContext(Dispatchers.IO) {
        val storageRef = FirebaseStorage.getInstance().reference
        val islandRef = storageRef.child("$IMAGES/$src")
        return@withContext islandRef.getBytes(ONE_MEGABYTE).asDeferred().await()
    }
}