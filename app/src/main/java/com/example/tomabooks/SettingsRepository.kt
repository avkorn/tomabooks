package com.example.tomabooks

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val FOLDER_URI_KEY = stringPreferencesKey("folder_uri")
    private val BOOKS_LIST_KEY = stringPreferencesKey("books_list")
    private val LAST_BOOK_URI_KEY = stringPreferencesKey("last_book_uri")
    private val LAST_POSITION_KEY = longPreferencesKey("last_position")
    private val REWIND_FORWARD_SECONDS_KEY = intPreferencesKey("rewind_forward_seconds")
    private val BLIND_MODE_KEY = booleanPreferencesKey("blind_mode")
    private val COMPLETED_BOOKS_KEY = stringSetPreferencesKey("completed_books")

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

    val rewindForwardSeconds: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[REWIND_FORWARD_SECONDS_KEY] ?: 20
    }

    val isBlindMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BLIND_MODE_KEY] ?: false
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

    suspend fun saveRewindForwardSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[REWIND_FORWARD_SECONDS_KEY] = seconds
        }
    }

    suspend fun saveBlindMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BLIND_MODE_KEY] = enabled
        }
    }

    val completedBooks: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[COMPLETED_BOOKS_KEY] ?: emptySet()
    }

    suspend fun saveCompletedBooks(completedSet: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[COMPLETED_BOOKS_KEY] = completedSet
        }
    }
}
