package com.client.traveller.data.network.api.wikipedia.response.wikipediaPageSummaryResponse


import com.google.gson.annotations.SerializedName

data class Titles(
    val canonical: String,
    val display: String,
    val normalized: String
)