package com.client.traveller.data.network.api.geocoding.response.geocodingResponse


import com.client.traveller.data.network.api.geocoding.response.geocodingResponse.AddressComponent
import com.client.traveller.data.network.api.geocoding.response.geocodingResponse.Geometry
import com.client.traveller.data.network.api.geocoding.response.geocodingResponse.PlusCode
import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("address_components")
    val addressComponents: List<AddressComponent>,
    @SerializedName("formatted_address")
    val formattedAddress: String,
    val geometry: Geometry,
    @SerializedName("place_id")
    val placeId: String,
    @SerializedName("plus_code")
    val plusCode: PlusCode,
    val types: List<String>
)