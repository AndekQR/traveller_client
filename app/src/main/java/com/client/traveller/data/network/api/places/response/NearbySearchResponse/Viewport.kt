package com.client.traveller.data.network.api.places.response.NearbySearchResponse


import com.google.gson.annotations.SerializedName

data class Viewport(
    val northeast: Northeast,
    val southwest: Southwest
)