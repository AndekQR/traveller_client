package com.client.traveller.data.network.api.wikipedia

import com.client.traveller.data.network.api.wikipedia.response.wikipediaPageSummaryResponse.WikipediaPageSummaryResponse
import com.client.traveller.data.network.api.wikipedia.response.wikipediaPrefixSearchResponse.WikipediaPrefixSearchResponse
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WikipediaApiService {

    @GET("w/api.php")
    suspend fun searchPrefixes(
        @Query("action") action: String = "query",
        @Query("list") list: String = "prefixsearch",
        @Query("pssearch") query: String,
        @Query("pslimmit") limit: Int = 6,
        @Query("format") format: String = "json"
    ): WikipediaPrefixSearchResponse

    @GET("api/rest_v1/page/summary/{pageTitle}")
    suspend fun getPageSummary(
        @Path(value = "pageTitle", encoded = false) pageTitle: String
    ): WikipediaPageSummaryResponse

    companion object {
        private const val BASE_URL = "https://pl.wikipedia.org/"

        private val gsonBuilder = GsonBuilder()
            .setLenient()
            .create()

        operator fun invoke(): WikipediaApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder))
                .build()
                .create(WikipediaApiService::class.java)
        }
    }
}