package com.client.traveller.data.network.api.wikipedia.response.wikipediaSectionsResponse


data class SectionX(
    val anchor: String,
    val id: Int,
    val isReferenceSection: Boolean,
    val line: String,
    val text: String,
    val toclevel: Int
)