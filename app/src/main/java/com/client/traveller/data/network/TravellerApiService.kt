package com.client.traveller.data.network

import com.client.traveller.data.network.response.LoginResponse
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface TravellerApiService {

    @POST("login")
    suspend fun userLogin(
        @Body data: Map<String, String>
    ):Response<LoginResponse>

    @POST("register")
    suspend fun register(
        @Body data: Map<String, String>
    ): Response<LoginResponse>

    companion object{
        operator fun invoke(
            networkInterceptor: NetworkInterceptorImpl
        ): TravellerApiService{

            val requestInterceptor = Interceptor {chain ->
                val client = chain.request()
                    .newBuilder()
                    .header("Content-Type", "application/json")
                    .build()

                return@Interceptor chain.proceed(client)
            }

            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            val okHttpClient = OkHttpClient.Builder()
                .addNetworkInterceptor(requestInterceptor)
                .addInterceptor(networkInterceptor)
                .addInterceptor(logging)
                .build()



           return Retrofit.Builder()
               .client(okHttpClient)
//               .baseUrl("https://traveller-server-andekqr.herokuapp.com/auth/")
               .baseUrl("http://192.168.43.215:8080/auth/")
               .addCallAdapterFactory(CoroutineCallAdapterFactory())
               .addConverterFactory(GsonConverterFactory.create())
               .build()
               .create(TravellerApiService::class.java)
        }
    }

}