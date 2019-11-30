package com.client.traveller.data.network.api.wikipedia.response.wikipediaPrefixSearchResponse


import com.google.gson.annotations.SerializedName

data class Prefixsearch(
    val ns: Int,
    val pageid: Int,
    val title: String
)