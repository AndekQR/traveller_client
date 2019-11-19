package com.client.traveller.data.repository.map

import android.content.Context
import android.location.Location
import android.os.Bundle
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.network.api.directions.model.TravelMode
import com.client.traveller.data.network.api.directions.response.Distance
import com.google.android.gms.maps.SupportMapFragment

interface MapRepository {
    fun initializeMap(
        mapFragment: SupportMapFragment,
        context: Context,
        savedInstanceState: Bundle?
    )

    fun startLocationUpdates()
    fun stopLocationUpdates()
    fun sendingLocationData(): Boolean

    fun centerCurrentLocation()
    suspend fun getDistance(origin: String, destination: String, waypoints: ArrayList<String>? = null, mode: TravelMode): Distance?
    fun getCurrentLocation(): Location
    fun clearMap()

    fun drawRouteMarker()
    fun drawRouteToLocation(
        origin: String,
        destination: String,
        locations: List<String>,
        mode: TravelMode
    )
    suspend fun drawTripRoute(trip: Trip)

}