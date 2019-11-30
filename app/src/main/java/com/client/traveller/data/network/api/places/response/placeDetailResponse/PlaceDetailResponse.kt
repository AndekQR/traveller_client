package com.client.traveller.data.network.api.places.response.placeDetailResponse


import com.google.gson.annotations.SerializedName

data class PlaceDetailResponse(
    val result: Result,
    val status: String
)