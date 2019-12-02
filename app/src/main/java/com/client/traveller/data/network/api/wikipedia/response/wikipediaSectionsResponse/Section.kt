package com.client.traveller.data.network.api.wikipedia.response.wikipediaSectionsResponse


data class Section(
    val anchor: String,
    val id: Int,
    val line: String,
    val text: String,
    val toclevel: Int
)