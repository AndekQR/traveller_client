package com.client.traveller.data.network.api.places.response.findPlacesResponse


data class FindPlacesResponse(
    val candidates: List<Candidate>,
    val status: String
)