package com.client.traveller.data.network.map.directions.response

import com.google.gson.annotations.SerializedName

data class Polyline(
    @SerializedName("points")
    val points: String = ""
)