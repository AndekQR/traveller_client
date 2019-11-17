package com.client.traveller.data.repository.place

import android.media.Image
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Photo
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result

interface PlacesRepository {

    suspend fun getNearbyPlaces(latlng: String? = null): Set<Result>
    fun getPhotoUrl(reference: String, width: Int): String
    fun getSearchedTypes(): List<String>
}