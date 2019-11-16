package com.client.traveller.data.network.api.places.response.findPlacesResponse


import com.google.gson.annotations.SerializedName

data class Candidate(
    @SerializedName("formatted_address")
    val formattedAddress: String,
    val name: String,
    @SerializedName("opening_hours")
    val openingHours: OpeningHours,
    val photos: List<Photo>,
    @SerializedName("place_id")
    val placeId: String,
    val rating: Double,
    val types: List<String>
)