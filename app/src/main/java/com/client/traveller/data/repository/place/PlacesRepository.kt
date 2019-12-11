package com.client.traveller.data.repository.place

import com.client.traveller.data.network.api.places.response.nearbySearchResponse.NearbySearchResponse
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
import com.client.traveller.data.network.api.places.response.placeDetailResponse.PlaceDetailResponse
import com.client.traveller.data.network.api.wikipedia.model.Section
import com.client.traveller.data.network.api.wikipedia.response.wikipediaPageSummaryResponse.WikipediaPageSummaryResponse
import com.client.traveller.data.network.api.wikipedia.response.wikipediaPrefixSearchResponse.WikipediaPrefixSearchResponse

interface PlacesRepository {

    suspend fun getNearbyPlaces(latlng: String, radius: Int? = null): Set<NearbySearchResponse>
    fun getPhotoUrl(reference: String, width: Int): String
    fun getSearchedTypes(): List<String>
    suspend fun getPlaceDetail(placeId: String): PlaceDetailResponse
    suspend fun getPrefixes(query: String): WikipediaPrefixSearchResponse
    suspend fun getPageSummary(pageTitle: String): WikipediaPageSummaryResponse
    suspend fun getPageSections(pageTitle: String): List<Section>
}