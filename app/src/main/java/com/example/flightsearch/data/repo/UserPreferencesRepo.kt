package com.example.flightsearch.data.repo

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map


/**
 * [UserPreferencesRepo] provides saving and retrieving PromptValue from DataStore
 */
class UserPreferencesRepo(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val PROMPT_VALUE = stringPreferencesKey("prompt_value")
        const val TAG = "UserPreferencesRepo"
    }

    suspend fun savePromptValue(promptValue: String) {
        dataStore.edit { preferences ->
            preferences[PROMPT_VALUE] = promptValue
        }
    }

    val promptValue: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emptyPreferences()
            } else {
                Log.e(TAG, "Uncaught error reading preferences", it)
                throw it
            }
        }.map { preferences ->
            preferences[PROMPT_VALUE] ?: ""
        }
}
