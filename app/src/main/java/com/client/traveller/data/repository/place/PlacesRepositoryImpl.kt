package com.client.traveller.data.repository.place

import com.client.traveller.data.network.api.places.API_KEY
import com.client.traveller.data.network.api.places.PlacesApiService
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
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
            val response = this.placesApiClient.findNearbyPlaces(latlng = location, type = type)
            val results = response.results
            listOfPlaces.addAll(results)
        }
        return listOfPlaces.toSet()
    }

    override fun getPhotoUrl(reference: String, width: Int): String {
        return "${PlacesApiService.BASE_URL}photo?maxwidth=${width}&photoreference=${reference}&key=$API_KEY"
    }

    override fun getSearchedTypes(): List<String> {
        return this.searchedTypes
    }

}