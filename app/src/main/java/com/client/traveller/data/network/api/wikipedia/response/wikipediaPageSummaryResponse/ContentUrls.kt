package com.client.traveller.data.network.api.wikipedia.response.wikipediaPageSummaryResponse


import com.google.gson.annotations.SerializedName

data class ContentUrls(
    val desktop: Desktop,
    val mobile: Mobile
)