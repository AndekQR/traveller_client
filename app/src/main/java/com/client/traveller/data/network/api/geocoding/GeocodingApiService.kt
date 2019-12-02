package com.client.traveller.data.network.api.geocoding

import com.client.traveller.data.network.api.geocoding.response.geocodingResponse.GeocodingResponse
import com.client.traveller.data.network.api.geocoding.response.reverseGeocodingResponse.ReverseGeocodingResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val API_KEY = "AIzaSyAd9mVHXtr7oJkIwt605x3Wu5A65srtq6Q"

interface GeocodingApiService {

    @GET("json")
    suspend fun geocode(
        @Query("address") address: String
    ): GeocodingResponse

    @GET("json")
    suspend fun reverseGeocode(
        @Query("latlng") latlng: String // lat,lng - bez spacji
    ): ReverseGeocodingResponse

    companion object {
        private const val BASE_URL = "https://maps.googleapis.com/maps/api/geocode/"

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

        operator fun invoke(): GeocodingApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GeocodingApiService::class.java)
        }
    }
}