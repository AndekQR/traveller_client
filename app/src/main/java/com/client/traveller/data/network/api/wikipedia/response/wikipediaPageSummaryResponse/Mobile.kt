package com.client.traveller.data.network.api.wikipedia.response.wikipediaPageSummaryResponse


import com.google.gson.annotations.SerializedName

data class Mobile(
    val edit: String,
    val page: String,
    val revisions: String,
    val talk: String
)