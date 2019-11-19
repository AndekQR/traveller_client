package com.client.traveller.data.network.api.geocoding.response.geocodingResponse


data class GeocodingResponse(
    val results: List<Result>,
    val status: String
)