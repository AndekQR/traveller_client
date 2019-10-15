package com.client.traveller.data.network.map.directions.response

import com.google.gson.annotations.SerializedName

data class OverviewPolyline(
    @SerializedName("points")
    val points: String = ""
)
