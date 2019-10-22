package com.client.traveller.data.network.map

import android.content.Context
import android.location.Location
import com.client.traveller.data.network.map.directions.model.TravelMode
import com.client.traveller.data.network.map.directions.response.Distance
import com.google.android.gms.maps.GoogleMap

interface MapUtils : GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener {
    fun initializeMap(context: Context)
    fun lastKnownLocation()
    fun drawRouteToMarker()
    fun drawRouteToLocation(origin: String, destination: String, locations: List<String>, mode: TravelMode)
    fun clearMap()
    fun centerCurrentLocation()
    suspend fun getDistance(origin: String, destination: String): Distance?
    fun getCurrentLocation(): Location
}