package com.example.flightsearch.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.flightsearch.data.model.Route
import com.example.flightsearch.data.model.Favorite
import kotlinx.coroutines.flow.Flow


@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favorite: Favorite)

    @Query("""
        DELETE FROM favorite
        WHERE departure_code = :departureCode
            AND destination_code = :destinationCode
    """)
    suspend fun delete(departureCode: String, destinationCode: String)

    @Query("""
        SELECT
            f.id,
            f.departure_code,
            a1.name as departure_name,
            f.destination_code,
            a2.name as destination_name
        FROM favorite AS f
        JOIN airport as a1 ON f.departure_code = a1.iata_code
        JOIN airport AS a2 ON  f.destination_code = a2.iata_code
    """)
    fun getAll(): Flow<List<Route>>
}
