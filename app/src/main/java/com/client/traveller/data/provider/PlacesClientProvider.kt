package com.client.traveller.data.provider

import android.content.Context
import com.client.traveller.R
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

object PlacesClientProvider {
    fun getClient(context: Context): PlacesClient {
        if (!Places.isInitialized()) {
            Places.initialize(
                context.applicationContext,
                context.getString(R.string.google_api_key)
            )
        }
        return Places.createClient(context)
    }
}