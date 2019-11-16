package com.client.traveller.data.network.api.directions.response

import com.google.gson.annotations.SerializedName

data class EndLocation(
    @SerializedName("lng")
    val lng: Double = 0.0,
    @SerializedName("lat")
    val lat: Double = 0.0
)
