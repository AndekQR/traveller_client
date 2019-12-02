package com.client.traveller.data.network.map

import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import com.client.traveller.data.network.api.directions.model.TravelMode
import com.client.traveller.data.network.api.directions.response.Distance
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline

interface MapUtils : GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMapLongClickListener {
    fun initializeMap(context: Context)
    fun lastKnownLocation()
    suspend fun drawRouteToMarker(marker: Marker?): Polyline?
    suspend fun drawRouteToLocation(
        origin: String,
        destination: String,
        locations: List<String>,
        mode: TravelMode
    )

    fun clearMap()
    fun centerCurrentLocation()
    suspend fun getDistance(
        origin: String,
        destination: String,
        waypoints: ArrayList<String>?
    ): Distance?

    fun drawMainMarker(position: LatLng): Marker?
    fun getCurrentLocation(): Location
    fun drawMarkerFromBitmap(position: LatLng, bitmap: Bitmap, toClear: Boolean = false)
    fun getMarkerFromMap(): Marker?
    fun centerCameraOnLocation(location: LatLng)
    fun centerCameraOnRoute(startAddress: LatLng, waypoints: ArrayList<LatLng>?, endAddress: LatLng)
    fun elementsOnMap(): Boolean
    fun drawPlaceMarkersInCluster(places: Set<Result>)
    fun disableMapDragging()
    fun enableMapDragging()
}