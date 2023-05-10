package com.m391.primavera.database.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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

    fun getUserUid(): Flow<String?> {
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val uploaded = preferences[currentUserUid]
            uploaded
        }
    }

    fun getUserType(): Flow<String?> {
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val uploaded = preferences[currentUserType]
            uploaded
        }
    }


    suspend fun setCurrentChildUid(uid: String) {
        dataStore.edit { preferences ->
            preferences[currentChildUid] = uid
        }
    }

    fun getCurrentChildUid(): Flow<String?> {
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val uid = preferences[currentChildUid]
            uid
        }
    }
}