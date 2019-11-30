package com.client.traveller.data.network.api.places.response.nearbySearchResponse


import com.google.gson.annotations.SerializedName

data class Result(
    val icon: String,
    val id: String,
    val name: String,
    @SerializedName("opening_hours")
    val openingHours: OpeningHours,
    val photos: List<Photo>,
    @SerializedName("place_id")
    val placeId: String,
    val rating: Double,
    val types: List<String>,
    val vicinity: String
)