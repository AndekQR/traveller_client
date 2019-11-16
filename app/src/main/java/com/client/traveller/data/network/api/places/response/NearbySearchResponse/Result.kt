package com.client.traveller.data.network.api.places.response.NearbySearchResponse


import com.google.gson.annotations.SerializedName

data class Result(
    val geometry: Geometry,
    val icon: String,
    val id: String,
    val name: String,
    @SerializedName("opening_hours")
    val openingHours: OpeningHours,
    val photos: List<Photo>,
    @SerializedName("place_id")
    val placeId: String,
    val rating: Double,
    val reference: String,
    val types: List<String>,
    @SerializedName("user_ratings_total")
    val userRatingsTotal: Int,
    val vicinity: String
)