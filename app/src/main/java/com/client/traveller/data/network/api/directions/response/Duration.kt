package com.client.traveller.data.network.api.directions.response

import com.google.gson.annotations.SerializedName

data class Duration(
    @SerializedName("text")
    val text: String = "",
    @SerializedName("value")
    val value: Int = 0
)