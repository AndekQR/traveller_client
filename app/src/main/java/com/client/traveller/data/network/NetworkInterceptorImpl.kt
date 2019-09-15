package com.client.traveller.data.network

import android.content.Context
import android.net.ConnectivityManager
import com.client.traveller.ui.util.NoInternetAvailableException
import okhttp3.Interceptor
import okhttp3.Response

class NetworkInterceptorImpl(context: Context) : NetworkInterceptor {

    private val applicationContext = context.applicationContext

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isInternetAvailable())
            throw NoInternetAvailableException("No internet connection!")

        return chain.proceed(chain.request())
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.activeNetworkInfo.also {
            return it != null && it.isConnected
        }
    }
}