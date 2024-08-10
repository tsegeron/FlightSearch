package com.example.flightsearch.data.repo

import com.example.flightsearch.data.model.Airport
import com.example.flightsearch.data.model.Favorite
import com.example.flightsearch.data.model.Route
import kotlinx.coroutines.flow.Flow


/**
 * Repository that provides insert, update, delete, and retrieve of [Favorite], [Airport]
 * from a database.
 */
interface FlightSearchRepository {
    /**
     * Retrieve all the [Favorite] from the the database.
     */
    fun getAllFavourites(): Flow<List<Route>>

    /**
     * Insert [Favorite] into the database
     */
    suspend fun insertFavourite(favorite: Favorite)

    /**
     * Insert [Favorite] from the database
     */
    suspend fun deleteFavourite(departureCode: String, destinationCode: String)

    /**
     * Retrieve all the [Airport] from the the database which [Airport.iataCode] or [Airport.name]
     * contains [prompt].
     */
    fun getAllAirportsByPrompt(prompt: String): Flow<List<Airport>>

    /**
     * Retrieve all the [Route]s from the the database from [Airport] whose [Airport.iataCode]
     * equals [iataCode].
     */
    suspend fun getAllRoutesFrom(iataCode: String): Flow<List<Route>>

}
