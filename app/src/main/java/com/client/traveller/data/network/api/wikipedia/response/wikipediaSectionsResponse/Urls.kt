package com.client.traveller.data.network.api.wikipedia.response.wikipediaSectionsResponse


import com.google.gson.annotations.SerializedName

data class Urls(
    @SerializedName("1024")
    val x1024: String,
    @SerializedName("320")
    val x320: String,
    @SerializedName("640")
    val x640: String,
    @SerializedName("800")
    val x800: String
)