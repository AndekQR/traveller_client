package com.client.traveller.data.network.api.wikipedia.response.wikipediaPrefixSearchResponse


import com.google.gson.annotations.SerializedName

data class WikipediaPrefixSearchResponse(
    val batchcomplete: String,
    @SerializedName("continue")
    val continueX: Continue,
    val query: Query
)