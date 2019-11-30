package com.client.traveller.data.network.api.wikipedia.response.wikipediaPageSummaryResponse


import com.google.gson.annotations.SerializedName

data class Thumbnail(
    val height: Int,
    val source: String,
    val width: Int
)