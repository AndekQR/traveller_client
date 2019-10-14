package com.client.traveller.data.repository.map

import android.content.Context
import android.os.Bundle
import com.google.android.gms.maps.SupportMapFragment

interface MapRepository{
    fun initializeMap(mapFragment: SupportMapFragment, context: Context, savedInstanceState: Bundle?)
    fun startLocationUpdates()
    fun stopLocationUpdates()
    fun sendingLocationData(): Boolean
}