package com.client.traveller.data.network.map

import android.content.Context
import android.location.Location
import com.google.android.gms.maps.GoogleMap

interface MapUtils : GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener {
    fun initializeMap(context: Context)
    fun lastKnownLocation()
    fun drawRouteToMarker()
    fun drawRouteToLocation(locations: List<Location>)
}