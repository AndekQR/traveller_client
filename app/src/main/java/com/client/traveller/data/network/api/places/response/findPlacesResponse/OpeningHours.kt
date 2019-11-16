package com.client.traveller.data.network.api.places.response.findPlacesResponse


import com.google.gson.annotations.SerializedName

data class OpeningHours(
    @SerializedName("open_now")
    val openNow: Boolean
)