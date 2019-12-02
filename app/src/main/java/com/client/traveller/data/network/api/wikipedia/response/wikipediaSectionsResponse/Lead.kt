package com.client.traveller.data.network.api.wikipedia.response.wikipediaSectionsResponse


import com.google.gson.annotations.SerializedName

data class Lead(
    val description: String,
    @SerializedName("description_source")
    val descriptionSource: String,
    val displaytitle: String,
    val editable: Boolean,
    val hatnotes: List<String>,
    val id: Int,
    val image: Image,
    val languagecount: Int,
    val lastmodified: String,
    val normalizedtitle: String,
    val ns: Int,
    val protection: Protection,
    val revision: String,
    val sections: List<Section>,
    @SerializedName("wikibase_item")
    val wikibaseItem: String
)