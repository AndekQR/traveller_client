package com.client.traveller.data.network.firebase.messaging.notifications

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationApiService {

    @POST("fcm/send")
    suspend fun sendNotification(
        @Body body: Sender
    ): Response

    companion object {
        private const val BASE_URL = "https://fcm.googleapis.com/"

        private val requestInterceptor = Interceptor {chain ->

            val request = chain.request()
                .newBuilder()
                .header("Content-Type", "application/json")
                .header("Authorization", "key=AAAAFvkTcpc:APA91bGtCTplyIGz-thZecHdqw1TJpWonJtfryot0B6-ORt-M-ahIDwwtSFORfzb3NLl_XUgAVrmWsdprozyUb104ej0sfGT7l2qYI-MOFwCBsu5wpB_cR_oZEbK-BDOqWE1eRjDemcN")
                .build()

            return@Interceptor chain.proceed(request)
        }

        private val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(requestInterceptor)
            .build()

        operator fun invoke() : NotificationApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NotificationApiService::class.java)
        }
    }
}