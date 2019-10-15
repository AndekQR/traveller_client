package com.client.traveller.data.network.map.directions.response

import com.google.gson.annotations.SerializedName
import com.google.maps.android.geometry.Bounds

data class RoutesItem(
    @SerializedName("summary")
    val summary: String? = "",
    @SerializedName("copyrights")
    val copyrights: String? = "",
    @SerializedName("legs")
    val legs: List<LegsItem>,
    @SerializedName("bounds")
    val bounds: Bounds?,
    @SerializedName("overview_polyline")
    val overviewPolyline: OverviewPolyline
)
