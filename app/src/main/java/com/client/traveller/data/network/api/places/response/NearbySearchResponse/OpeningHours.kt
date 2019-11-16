package com.client.traveller.data.network.api.places.response.NearbySearchResponse


import com.google.gson.annotations.SerializedName

data class OpeningHours(
    @SerializedName("open_now")
    val openNow: Boolean
)