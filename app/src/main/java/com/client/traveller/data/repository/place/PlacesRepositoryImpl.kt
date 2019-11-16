package com.client.traveller.data.repository.place

import com.client.traveller.data.network.api.places.PlacesApiService
import com.client.traveller.data.network.api.places.response.NearbySearchResponse.NearbySearchResponse
import com.client.traveller.data.network.api.places.response.NearbySearchResponse.Result
import com.client.traveller.data.network.api.places.response.findPlacesResponse.FindPlacesResponse
import com.client.traveller.data.provider.LocationProvider

class PlacesRepositoryImpl(
    private val locationProvider: LocationProvider,
    private val placesApiClient: PlacesApiService
) : PlacesRepository {

    private val searchedTypes = listOf(
        "restaurant",
        "amusement_park",
        "aquarium",
        "art_gallery",
        "bar",
        "bowling_alley",
        "cafe",
        "casino",
        "church",
        "library",
        "museum",
        "park",
        "tourist_attraction",
        "zoo"
    )


    override suspend fun getNearbyPlaces(latlng: String?): Set<Result> {
        val location = latlng ?: "${locationProvider.currentLocation?.latitude},${locationProvider.currentLocation?.longitude}"
        val listOfPlaces = mutableSetOf<Result>()
        this.searchedTypes.forEach {type ->
            listOfPlaces.addAll(this.placesApiClient.findNearbyPlaces(latlng = location, type = type).results)
        }
        return listOfPlaces.toSet()
    }
}