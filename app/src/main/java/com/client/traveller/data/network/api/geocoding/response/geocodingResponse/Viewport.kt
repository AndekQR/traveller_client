package com.client.traveller.data.network.api.geocoding.response.geocodingResponse


import com.client.traveller.data.network.api.geocoding.response.geocodingResponse.Northeast
import com.client.traveller.data.network.api.geocoding.response.geocodingResponse.Southwest

data class Viewport(
    val northeast: Northeast,
    val southwest: Southwest
)