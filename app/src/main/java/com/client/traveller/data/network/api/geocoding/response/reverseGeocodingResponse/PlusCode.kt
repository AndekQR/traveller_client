package com.client.traveller.data.network.api.geocoding.response.reverseGeocodingResponse


import com.google.gson.annotations.SerializedName

data class PlusCode(
    @SerializedName("compound_code")
    val compoundCode: String,
    @SerializedName("global_code")
    val globalCode: String
)