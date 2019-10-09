package com.client.traveller.data.network.map

import com.google.android.gms.maps.GoogleMap

interface MapUtils: GoogleMap.OnMapClickListener {
    fun lastKnownLocation()
}