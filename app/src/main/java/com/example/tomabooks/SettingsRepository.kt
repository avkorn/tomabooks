package com.example.tomabooks

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val FOLDER_URI_KEY = stringPreferencesKey("folder_uri")
    private val BOOKS_LIST_KEY = stringPreferencesKey("books_list")
    private val LAST_BOOK_URI_KEY = stringPreferencesKey("last_book_uri")
    private val LAST_POSITION_KEY = longPreferencesKey("last_position")

    val folderUri: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[FOLDER_URI_KEY]
    }

    val booksList: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[BOOKS_LIST_KEY]
    }

    val lastBookUri: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LAST_BOOK_URI_KEY]
    }

    val lastPosition: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_POSITION_KEY] ?: 0L
    }

    suspend fun saveFolderUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[FOLDER_URI_KEY] = uri
        }
    }

    suspend fun saveBooksList(booksJson: String) {
        context.dataStore.edit { preferences ->
            preferences[BOOKS_LIST_KEY] = booksJson
        }
    }

    suspend fun saveLastPlayback(uri: String, position: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_BOOK_URI_KEY] = uri
            preferences[LAST_POSITION_KEY] = position
        }
    }
}
