package com.client.traveller.data.repository.place

import com.client.traveller.data.network.api.places.response.NearbySearchResponse.NearbySearchResponse
import com.client.traveller.data.network.api.places.response.NearbySearchResponse.Result
import com.client.traveller.data.network.api.places.response.findPlacesResponse.FindPlacesResponse

interface PlacesRepository {

    suspend fun getNearbyPlaces(latlng: String? = null): Set<Result>
}