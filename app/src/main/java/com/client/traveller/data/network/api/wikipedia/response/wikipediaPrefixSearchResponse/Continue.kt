package com.client.traveller.data.network.api.wikipedia.response.wikipediaPrefixSearchResponse


import com.google.gson.annotations.SerializedName

data class Continue(
    @SerializedName("continue")
    val continueX: String,
    val psoffset: Int
)