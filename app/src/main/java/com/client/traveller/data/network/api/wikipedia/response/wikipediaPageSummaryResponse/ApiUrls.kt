package com.client.traveller.data.network.api.wikipedia.response.wikipediaPageSummaryResponse


import com.google.gson.annotations.SerializedName

data class ApiUrls(
    @SerializedName("edit_html")
    val editHtml: String,
    val media: String,
    val metadata: String,
    val references: String,
    val summary: String,
    @SerializedName("talk_page_html")
    val talkPageHtml: String
)