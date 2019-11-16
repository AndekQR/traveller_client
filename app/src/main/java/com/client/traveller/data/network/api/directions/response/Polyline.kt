package com.client.traveller.data.network.api.directions.response

import com.google.gson.annotations.SerializedName

data class Polyline(
    @SerializedName("points")
    val points: String = ""
)