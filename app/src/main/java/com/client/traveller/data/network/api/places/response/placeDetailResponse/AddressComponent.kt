package com.client.traveller.data.network.api.places.response.placeDetailResponse


import com.google.gson.annotations.SerializedName

data class AddressComponent(
    @SerializedName("long_name")
    val longName: String,
    val types: List<String>
)