package com.client.traveller.data.network.api.wikipedia.response.wikipediaPrefixSearchResponse


import com.google.gson.annotations.SerializedName

data class Query(
    val prefixsearch: List<Prefixsearch>
)