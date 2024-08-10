package com.example.flightsearch.data.repo

import com.example.flightsearch.data.dao.AirportDao
import com.example.flightsearch.data.dao.FavoriteDao
import com.example.flightsearch.data.model.Airport
import com.example.flightsearch.data.model.Favorite
import com.example.flightsearch.data.model.Route
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow


/**
 * [FlightSearchRepository] interface implementation
 */
class FlightSearchRepositoryImpl(
    private val airportDao: AirportDao,
    private val favoriteDao: FavoriteDao
): FlightSearchRepository {
    override fun getAllFavourites(): Flow<List<Route>> = favoriteDao.getAll()

    override suspend fun insertFavourite(favorite: Favorite) = favoriteDao.insert(favorite)

    override suspend fun deleteFavourite(departureCode: String, destinationCode: String) =
        favoriteDao.delete(departureCode, destinationCode)

    override fun getAllAirportsByPrompt(prompt: String): Flow<List<Airport>> =
        airportDao.getByPrompt(prompt)

    override suspend fun getAllRoutesFrom(iataCode: String): Flow<List<Route>> = flow {
        val departureAirport = airportDao.getByIataCode(iataCode).first()
        val destinationAirportsList = airportDao.getAllExcluding(iataCode).first()

        emit(
            destinationAirportsList.map {
                Route(
                    id = it.id,
                    departureCode = departureAirport.iataCode,
                    departureName = departureAirport.name,
                    destinationCode = it.iataCode,
                    destinationName = it.name
                )
            }
        )
    }
}
