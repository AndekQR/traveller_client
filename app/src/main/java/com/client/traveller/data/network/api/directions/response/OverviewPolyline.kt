package com.client.traveller.data.network.api.directions.response

import com.google.gson.annotations.SerializedName

data class OverviewPolyline(
    @SerializedName("points")
    val points: String = ""
)
