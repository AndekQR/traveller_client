package com.client.traveller.data.repository.map

import android.content.Context
import android.location.Location
import android.os.Bundle
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.network.api.directions.model.TravelMode
import com.client.traveller.data.network.api.directions.response.Distance
import com.client.traveller.data.network.api.geocoding.response.geocodingResponse.GeocodingResponse
import com.client.traveller.data.network.api.geocoding.response.reverseGeocodingResponse.ReverseGeocodingResponse
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.NearbySearchResponse
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline

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
    suspend fun getDistance(
        origin: String,
        destination: String,
        waypoints: ArrayList<String>? = null,
        mode: TravelMode
    ): Distance?

    fun getCurrentLocation(): Location
    fun clearMap()

    suspend fun drawRouteToMainMarker(): Polyline?
    suspend fun drawRouteToLocation(
        origin: String,
        destination: String,
        locations: List<String>?,
        mode: TravelMode
    )

    suspend fun drawTripRoute(trip: Trip, travelMode: TravelMode = TravelMode.driving)
    fun centerCameraOnLocation(location: LatLng, putMarker: Boolean = false)
    suspend fun centerCameraOnRoute(startAddress: String, waypoints: ArrayList<String>?, endAddress: String)
    fun elementOnMap(): Boolean
    suspend fun drawNearbyPlaceMarkers(places: Set<NearbySearchResponse>)
    fun getPhotoUrl(reference: String, width: Int): String
    fun getActualMarker(): Marker?
    fun disableMapDragging()
    suspend fun geocodeAddress(address: String): GeocodingResponse
    suspend fun reverseGeocoding(latlng: String): ReverseGeocodingResponse
}