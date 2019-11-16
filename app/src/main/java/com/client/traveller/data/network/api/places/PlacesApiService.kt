package com.client.traveller.data.network.api.places

import com.client.traveller.data.network.api.places.response.NearbySearchResponse.NearbySearchResponse
import com.client.traveller.data.network.api.places.response.findPlacesResponse.FindPlacesResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val API_KEY = "AIzaSyAd9mVHXtr7oJkIwt605x3Wu5A65srtq6Q"

interface PlacesApiService {

    @GET("nearbysearch/json")
    suspend fun findNearbyPlaces(
        @Query("location") latlng: String, // as latitude,longitude.
        @Query("radius") radius: Int = 3000, //max 50 000
        @Query("language") language: String = "pl",
        @Query("type") type: String,
        @Query("keyword") keyword: String = ""
    ): NearbySearchResponse


    companion object {
        private const val BASE_URL = "https://maps.googleapis.com/maps/api/place/"

        private val requestInterceptor = Interceptor { chain ->
            val url = chain.request()
                .url()
                .newBuilder()
                .addQueryParameter("key", API_KEY)
                .build()

            val request = chain.request()
                .newBuilder()
                .url(url)
                .build()

            return@Interceptor chain.proceed(request)
        }

        private val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(requestInterceptor)
            .build()

        operator fun invoke(): PlacesApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PlacesApiService::class.java)
        }
    }
}