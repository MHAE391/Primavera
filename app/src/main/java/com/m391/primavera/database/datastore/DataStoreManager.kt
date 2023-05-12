package com.m391.primavera.database.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreManager(context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Primavera")
    private val dataStore = context.dataStore

    companion object {
        val currentUserUid = stringPreferencesKey("CURRENT_USER_DATA_KEY")
        val currentUserType = stringPreferencesKey("CURRENT_USER_TYPE_KEY")
        val currentChildUid = stringPreferencesKey("CURRENT_CHILD_UID")

        @Volatile
        private var instance: DataStoreManager? = null

        fun getInstance(context: Context): DataStoreManager {
            return instance ?: synchronized(this) {
                instance ?: DataStoreManager(context).also { instance = it }
            }
        }
    }


    suspend fun setUserUid(uid: String) {
        dataStore.edit { preferences ->
            preferences[currentUserUid] = uid
        }
    }

    suspend fun setUserType(type: String) {
        dataStore.edit { preferences ->
            preferences[currentUserType] = type
        }
    }

    suspend fun getUserUid(): String? {
        val preferences = dataStore.data.first()
        return preferences[currentUserUid]
    }

    suspend fun getUserType(): String? {
        val preferences = dataStore.data.first()
        return preferences[currentUserType]
    }

    fun userType(): Flow<String?> {
        return dataStore.data.map { pre ->
            pre[currentUserType]
        }
    }


    suspend fun setCurrentChildUid(uid: String) {
        dataStore.edit { preferences ->
            preferences[currentChildUid] = uid
        }
    }

    suspend fun getCurrentChildUid(): String? {
        val preferences = dataStore.data.first()
        return preferences[currentChildUid]
    }
}