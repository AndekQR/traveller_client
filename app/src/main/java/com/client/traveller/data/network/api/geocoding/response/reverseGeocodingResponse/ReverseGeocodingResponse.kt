package com.client.traveller.data.network.api.geocoding.response.reverseGeocodingResponse


import com.google.gson.annotations.SerializedName

data class ReverseGeocodingResponse(
    @SerializedName("plus_code")
    val plusCode: PlusCode,
    val results: List<Result>,
    val status: String
)