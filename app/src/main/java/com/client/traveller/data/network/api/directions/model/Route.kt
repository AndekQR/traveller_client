package com.client.traveller.data.network.api.directions.model

data class Route(
    val startLat: Double?,
    val startLng: Double?,
    val endLat: Double?,
    val endLng: Double?,
    val overviewPolyline: String = ""
)