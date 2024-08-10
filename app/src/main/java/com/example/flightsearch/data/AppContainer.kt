package com.example.flightsearch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.flightsearch.data.repo.FlightSearchRepository
import com.example.flightsearch.data.repo.FlightSearchRepositoryImpl
import com.example.flightsearch.data.repo.UserPreferencesRepo


private const val VALUES_PREFERENCE_NAME = "values_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = VALUES_PREFERENCE_NAME
)

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val flightSearchRepository: FlightSearchRepository
    val userPreferencesRepo: UserPreferencesRepo
}

/**
 * [AppContainer] implementation that provides instance of [FlightSearchRepositoryImpl]
 * and [UserPreferencesRepo]
 */
class AppDataContainer(private val context: Context): AppContainer {
    override val flightSearchRepository: FlightSearchRepository by lazy {
        FlightSearchRepositoryImpl(
            AppDatabase.getDatabase(context).airportDao(),
            AppDatabase.getDatabase(context).favouriteDao()
        )
    }
    override val userPreferencesRepo: UserPreferencesRepo by lazy {
        UserPreferencesRepo(context.dataStore)
    }
}
