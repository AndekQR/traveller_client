package com.client.traveller.data.network.api.places.response.NearbySearchResponse


import com.google.gson.annotations.SerializedName

data class Geometry(
    val location: Location,
    val viewport: Viewport
)