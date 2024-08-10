package com.example.flightsearch.ui

import androidx.compose.ui.util.fastAny
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flightsearch.FlightSearchApplication
import com.example.flightsearch.data.model.Airport
import com.example.flightsearch.data.model.Favorite
import com.example.flightsearch.data.model.Route
import com.example.flightsearch.data.repo.FlightSearchRepository
import com.example.flightsearch.data.repo.UserPreferencesRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/**
 * Holds app states
 */
enum class FlightSearchAppState {
    SEARCH_RESULTS, FLIGHTS_FROM, FAVOURITES
}

/**
 * Ui State for the app
 */
data class FlightSearchUiState(
    var prompt: String = "",
    val favouritesList: List<Route> = emptyList(),
    val routesList: List<Route> = emptyList(),
    val appState: FlightSearchAppState = FlightSearchAppState.FAVOURITES
)

/**
 * ViewModel to manage all changes of ui and update DataStore and Database
 */
class FlightSearchViewModel(
    private val flightSearchRepository: FlightSearchRepository,
    private val userPreferencesRepo: UserPreferencesRepo
): ViewModel() {
    private val _uiState = MutableStateFlow(FlightSearchUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val promptValue: String = userPreferencesRepo.promptValue.first()
            val favouriteList = flightSearchRepository.getAllFavourites().first()
            val routesList = if (promptValue.isNotEmpty()) {
                flightSearchRepository.getAllRoutesFrom(promptValue).first()
            } else {
                emptyList()
            }
            val appState = if (promptValue.isNotEmpty()) {
                FlightSearchAppState.FLIGHTS_FROM
            } else {
                FlightSearchAppState.FAVOURITES
            }
            _uiState.update {
                it.copy(
                    prompt = promptValue,
                    favouritesList = favouriteList,
                    routesList = routesList,
                    appState = appState
                )
            }
        }
    }

    private suspend fun savePromptValueToDataStore(prompt: String) =
        userPreferencesRepo.savePromptValue(prompt)

    fun addRemoveFavourite(favourite: Favorite) {
        viewModelScope.launch {
            val isInFavoriteList: Boolean = _uiState.value.favouritesList
                .fastAny {
                    favourite.departureCode == it.departureCode &&
                            favourite.destinationCode == it.destinationCode
                }

            if (isInFavoriteList) {
                flightSearchRepository.deleteFavourite(favourite.departureCode, favourite.destinationCode)
            } else {
                flightSearchRepository.insertFavourite(favourite)
            }

            val favouriteList = flightSearchRepository.getAllFavourites().first()

            _uiState.update {
                it.copy(
                    favouritesList = favouriteList,
                )
            }
        }
    }

    fun updatePrompt(iataCodePrompt: String) {
        if (iataCodePrompt.isEmpty()) {
            _uiState.update { it.copy(prompt = iataCodePrompt) }
        } else {
            viewModelScope.launch {
                val routesList = flightSearchRepository.getAllRoutesFrom(iataCodePrompt).first()

                savePromptValueToDataStore(iataCodePrompt)

                _uiState.update {
                    it.copy(
                        prompt = iataCodePrompt,
                        routesList = routesList,
                    )
                }
            }
        }
    }

    fun getAllAirportsByPrompt(prompt: String): Flow<List<Airport>> =
        flightSearchRepository.getAllAirportsByPrompt(prompt)

    fun updateAppState(state: FlightSearchAppState) {
        _uiState.update {
            it.copy(
                appState = state
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                FlightSearchViewModel(
                    inventoryApplication().container.flightSearchRepository,
                    inventoryApplication().container.userPreferencesRepo
                )
            }
        }
    }
}

fun CreationExtras.inventoryApplication(): FlightSearchApplication =
    (this[APPLICATION_KEY] as FlightSearchApplication)
