package com.example.flightsearch.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.flightsearch.data.model.Airport
import kotlinx.coroutines.flow.Flow


@Dao
interface AirportDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(airport: Airport)

    @Query("""
        SELECT * FROM airport
        WHERE iata_code = :iataCode
    """)
    fun getByIataCode(iataCode: String): Flow<Airport>

    @Query("""
        SELECT * FROM airport
        WHERE iata_code NOT LIKE :iataCode
        ORDER BY passengers DESC
    """)
    fun getAllExcluding(iataCode: String): Flow<List<Airport>>

    @Query("""
        SELECT * FROM airport
        WHERE iata_code LIKE '%' || :prompt || '%'
            OR name LIKE '%' || :prompt || '%'
        ORDER BY passengers DESC
    """)
    fun getByPrompt(prompt: String): Flow<List<Airport>>
}
