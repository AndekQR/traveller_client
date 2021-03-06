package com.client.traveller.data.network.api.places.response.nearbySearchResponse


import com.google.gson.annotations.SerializedName

data class NearbySearchResponse(
    @SerializedName("next_page_token")
    val nextPageToken: String,
    val results: List<Result>,
    val status: String
)