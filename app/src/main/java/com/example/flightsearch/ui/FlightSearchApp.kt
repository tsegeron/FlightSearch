package com.example.flightsearch.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flightsearch.R
import com.example.flightsearch.data.model.Airport
import com.example.flightsearch.data.model.Favorite
import com.example.flightsearch.data.model.Route
import com.example.flightsearch.data.model.toFavorite
import com.example.flightsearch.ui.theme.FlightSearch1Theme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchApp(
    viewModel: FlightSearchViewModel = viewModel(factory = FlightSearchViewModel.Factory)
) {
    val appUiState by viewModel.uiState.collectAsState()
    var textFieldValue by remember(appUiState.prompt) {
        mutableStateOf(TextFieldValue(appUiState.prompt))
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(id = R.color.top_app_bar_container)),
                modifier = Modifier.shadow(elevation = dimensionResource(id = R.dimen.top_app_bar_elevation))
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(
                    top = innerPadding.calculateTopPadding() + dimensionResource(id = R.dimen.scaffold_content_padding),
                    start = dimensionResource(id = R.dimen.scaffold_content_padding),
                    bottom = innerPadding.calculateBottomPadding(),
                    end = dimensionResource(id = R.dimen.scaffold_content_padding)
                )
                .fillMaxSize()
        ) {
            SearchTextField(
                textFieldValue = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    if (textFieldValue.text.isNotEmpty() &&
                        appUiState.appState != FlightSearchAppState.SEARCH_RESULTS)
                        viewModel.updateAppState(FlightSearchAppState.SEARCH_RESULTS)
                },
                onClearIconClick = {
                    viewModel.updatePrompt("")
                    viewModel.updateAppState(FlightSearchAppState.FAVOURITES)
                    textFieldValue = TextFieldValue()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(id = R.dimen.search_text_field_bottom_padding))
            )

            when (appUiState.appState) {
                FlightSearchAppState.SEARCH_RESULTS -> {
                    val airportsList by viewModel.getAllAirportsByPrompt(textFieldValue.text)
                        .collectAsState(initial = emptyList())

                    SearchResultsLazyColumn(
                        airportsList = airportsList,
                        onCardClick = {
                            viewModel.updatePrompt(it)
                            viewModel.updateAppState(FlightSearchAppState.FLIGHTS_FROM)
                            keyboardController?.hide()
                            focusManager.clearFocus(force = true)
                            textFieldValue = TextFieldValue(it)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
                FlightSearchAppState.FLIGHTS_FROM -> {
                    Text(
                        text = stringResource(id = R.string.flights_from, appUiState.prompt),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_medium))
                    )
                    RoutesLazyColumn(
                        routesList = appUiState.routesList,
                        favouriteRoutesList = appUiState.favouritesList,
                        onCardClick = viewModel::addRemoveFavourite
                    )
                }
                FlightSearchAppState.FAVOURITES -> {
                    Text(
                        text = stringResource(id = R.string.favourite_routes),
                        fontWeight = FontWeight.Bold,
                    )
                    RoutesLazyColumn(
                        routesList = appUiState.favouritesList,
                        favouriteRoutesList = appUiState.favouritesList,
                        onCardClick = viewModel::addRemoveFavourite
                    )
                }
            }
        }
    }
}

@Composable
fun SearchTextField(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onClearIconClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    TextField(
        value = textFieldValue,
        onValueChange = onValueChange,
        modifier = modifier,
        shape = Shapes().extraLarge,
        singleLine = true,
        placeholder = { Text(stringResource(id = R.string.search_placeholder)) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colorResource(id = R.color.text_field_focused_container),
            unfocusedContainerColor = colorResource(id = R.color.text_field_unfocused_container),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        keyboardOptions = KeyboardOptions().copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                focusManager.clearFocus(force = true)
            }
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(id = R.string.search)
            )
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus(force = true)
                    onClearIconClick()
                },
                enabled = textFieldValue.text.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(id = R.string.clear)
                )
            }
        }
    )
}

@Composable
fun SearchResultsLazyColumn(
    airportsList: List<Airport>,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(airportsList) { airport ->
            Card(
                colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.airport_row_card_container)),
                shape = RectangleShape,
                onClick = { onCardClick(airport.iataCode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(id = R.dimen.card_bottom_padding_medium))
            ) {
                AirportRow(
                    name = airport.name,
                    iataCode = airport.iataCode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(id = R.dimen.airport_row_horizontal_padding))
                )
            }
        }
    }
}

@Composable
fun RoutesLazyColumn(
    routesList: List<Route>,
    favouriteRoutesList: List<Route>,
    onCardClick: (Favorite) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
        modifier = modifier
    ) {
        items(routesList) { route ->
            RouteRow(
                route = route,
                isFavourite = route in favouriteRoutesList,
                onCardClick = onCardClick,
            )
        }
    }
}

@Composable
fun RouteRow(
    route: Route,
    isFavourite: Boolean,
    onCardClick: (Favorite) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.route_row_card_container)),
        shape = RoundedCornerShape(topEnd = 12.dp, bottomStart = 12.dp),
        onClick = { onCardClick(route.toFavorite()) },
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_large))
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.depart).uppercase(),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                AirportRow(
                    name = route.departureName,
                    iataCode = route.departureCode
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_small)))
                Text(
                    text = stringResource(id = R.string.arrive).uppercase(),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                AirportRow(
                    name = route.destinationName,
                    iataCode = route.destinationCode
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_medium))
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    tint = if (isFavourite) colorResource(id = R.color.favorite_icon_marked) else Color.Gray,
                    contentDescription = stringResource(id = R.string.favourite)
                )
            }
        }
    }
}

@Composable
fun AirportRow(
    name: String,
    iataCode: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Text(
            text = iataCode,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(dimensionResource(id = R.dimen.iata_code_width))
        )
        Spacer(Modifier.width(dimensionResource(id = R.dimen.spacer_medium)))
        Text(
            text = name,
            fontWeight = FontWeight.Light,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            maxLines = 2
        )
    }
}


@Preview(showBackground = true)
@Composable
fun RouteRowPreview() {
    FlightSearch1Theme {
        RouteRow(
            route = Route(1, "SVO", "Sheremetyevo - A.S. Pushkin international", "FCO", "Leonardo da Vinci International Airport"),
            onCardClick = {},
            isFavourite = false,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RoutesLazyColumnPreview() {
    val mockRoutesList = listOf(
        Route(1, "SVO", "Sheremetyevo - A.S. Pushkin international", "TXL", "Berlin-Tegel Airport"),
        Route(2, "SVO", "Sheremetyevo - A.S. Pushkin international", "FCO", "Leonardo da Vinci International Airport"),
        Route(3, "SVO", "Sheremetyevo - A.S. Pushkin international", "MUC", "Munich International Airport"),
    )
    val mockFavouriteRoutesList = listOf(
        Route(1, "SVO", "Sheremetyevo - A.S. Pushkin international", "FCO", "Leonardo da Vinci International Airport"),
    )

    FlightSearch1Theme {
        RoutesLazyColumn(
            routesList = mockRoutesList,
            favouriteRoutesList = mockFavouriteRoutesList,
            onCardClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
