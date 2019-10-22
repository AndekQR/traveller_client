package com.client.traveller.data.network.map.directions

import com.client.traveller.data.network.map.directions.model.TravelMode
import com.client.traveller.data.network.map.directions.response.Directions
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val API_KEY = "AIzaSyAd9mVHXtr7oJkIwt605x3Wu5A65srtq6Q"

interface DirectionsApiService {

    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String = TravelMode.driving.name,
        @Query("units") units : String = "metric"
    ): Directions

    @GET("directions/json")
    suspend fun getDirectionsWithWaypoints(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String = TravelMode.driving.name,
        @Query("waypoints") waypoints: List<String>,
        @Query("units") units : String = "metric"
    ): Directions


    companion object {

        private const val GOOGLE_BASE_URL = "https://maps.googleapis.com/maps/api/"

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

        operator fun invoke(): DirectionsApiService {
            return Retrofit.Builder()
                .baseUrl(GOOGLE_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DirectionsApiService::class.java)
        }
    }
}