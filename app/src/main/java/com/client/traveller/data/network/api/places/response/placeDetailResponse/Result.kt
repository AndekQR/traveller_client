package com.client.traveller.data.network.api.places.response.placeDetailResponse


import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Photo
import com.google.gson.annotations.SerializedName

data class Result(
//    @SerializedName("address_components")
//    val addressComponents: List<AddressComponent>,
    @SerializedName("formatted_address")
    val formattedAddress: String,
    @SerializedName("formatted_phone_number")
    val formattedPhoneNumber: String,
    val geometry: Geometry,
    val icon: String,
    val id: String,
    val name: String,
    @SerializedName("place_id")
    val placeId: String,
    val rating: Double,
    val photos: List<Photo>,
//    val reviews: List<Review>,
    val types: List<String>,
//    val url: String,
    @SerializedName("utc_offset")
    val utcOffset: Int,
    val vicinity: String,
    val website: String
)