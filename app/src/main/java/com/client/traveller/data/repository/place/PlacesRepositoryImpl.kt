package com.client.traveller.data.repository.place

import com.client.traveller.data.network.api.places.API_KEY
import com.client.traveller.data.network.api.places.PlacesApiService
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
import com.client.traveller.data.network.api.places.response.placeDetailResponse.PlaceDetailResponse
import com.client.traveller.data.network.api.wikipedia.WikipediaApiService
import com.client.traveller.data.network.api.wikipedia.response.wikipediaPageSummaryResponse.WikipediaPageSummaryResponse
import com.client.traveller.data.network.api.wikipedia.response.wikipediaPrefixSearchResponse.WikipediaPrefixSearchResponse
import com.client.traveller.data.provider.LocationProvider

class PlacesRepositoryImpl(
    private val locationProvider: LocationProvider,
    private val placesApiClient: PlacesApiService,
    private val wikipediaApiService: WikipediaApiService
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

    //TODO switch map lokalna baza i places api, trzeba dodać jakiś interceptor czy jest internet
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

    override suspend fun getPlaceDetail(placeId: String): PlaceDetailResponse {
        return placesApiClient.getPlaceDetail(placeId)
    }

    /**
     * Zwraca tablicę obiektów które mają numer strony wikipedii i tytuł strony
     */
    override suspend fun getPrefixes(query: String): WikipediaPrefixSearchResponse {
        return this.wikipediaApiService.searchPrefixes(query = query)
    }

    /**
     * otrzymujemy opis zagadnienia ze strony z podanym tytułem
     */
    override suspend fun getPageSummary(pageTitle: String): WikipediaPageSummaryResponse {
        return this.wikipediaApiService.getPageSummary(pageTitle)
    }

}