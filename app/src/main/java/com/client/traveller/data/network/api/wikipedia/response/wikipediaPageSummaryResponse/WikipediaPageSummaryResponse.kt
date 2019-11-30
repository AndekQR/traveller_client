package com.client.traveller.data.network.api.wikipedia.response.wikipediaPageSummaryResponse


import com.google.gson.annotations.SerializedName

data class WikipediaPageSummaryResponse(
    @SerializedName("api_urls")
    val apiUrls: ApiUrls,
    @SerializedName("content_urls")
    val contentUrls: ContentUrls,
    val description: String,
    val dir: String,
    val displaytitle: String,
    val extract: String,
    @SerializedName("extract_html")
    val extractHtml: String,
    val lang: String,
    val namespace: Namespace,
    val originalimage: Originalimage,
    val pageid: Int,
    val revision: String,
    val thumbnail: Thumbnail,
    val tid: String,
    val timestamp: String,
    val title: String,
    val titles: Titles,
    val type: String,
    @SerializedName("wikibase_item")
    val wikibaseItem: String
)