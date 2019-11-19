package com.client.traveller.data.network.api.geocoding.response.reverseGeocodingResponse


import com.google.gson.annotations.SerializedName

data class Geometry(
    val bounds: Bounds,
    val location: Location,
    @SerializedName("location_type")
    val locationType: String,
    val viewport: Viewport
)