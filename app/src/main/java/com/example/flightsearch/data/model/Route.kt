package com.example.flightsearch.data.model

import androidx.room.ColumnInfo


/**
 * Data class for getting Favorite [Route]s as a representable data
 * from [Favorite] and [Airport] tables in Database
 */
data class Route(
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "departure_code")
    val departureCode: String = "",
    @ColumnInfo(name = "departure_name")
    val departureName: String = "",
    @ColumnInfo(name = "destination_code")
    val destinationCode: String = "",
    @ColumnInfo(name = "destination_name")
    val destinationName: String = ""
)

fun Route.toFavorite(): Favorite = Favorite(
    id = id,
    departureCode = departureCode,
    destinationCode = destinationCode
)
