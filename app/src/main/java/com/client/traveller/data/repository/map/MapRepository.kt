package com.client.traveller.data.repository.map

import android.content.Context
import android.location.Location
import android.os.Bundle
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.api.directions.model.TravelMode
import com.client.traveller.data.network.api.directions.response.Distance
import com.client.traveller.data.network.api.geocoding.response.geocodingResponse.GeocodingResponse
import com.client.traveller.data.network.api.geocoding.response.reverseGeocodingResponse.ReverseGeocodingResponse
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
import com.client.traveller.data.network.firebase.firestore.model.UserLocalization
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

interface MapRepository {

    fun initializeMap(
        mapFragment: SupportMapFragment,
        context: Context,
        savedInstanceState: Bundle?,
        locationToCenter: LatLng? = null
    )

    suspend fun getDistance(
        origin: String,
        destination: String,
        waypoints: ArrayList<String>? = null,
        mode: TravelMode
    ): Distance?

    fun clearMap()

    suspend fun drawRouteToMainMarker(location: Location)
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
    suspend fun drawNearbyPlaceMarkers(places: Set<Result>)
    fun getPhotoUrl(reference: String, width: Int): String
    fun getActualMarker(): Marker?
    fun disableMapDragging()
    suspend fun geocodeAddress(address: String): GeocodingResponse
    suspend fun reverseGeocoding(latlng: String): ReverseGeocodingResponse
    fun sendNewLocation(userLocalization: UserLocalization, trip: Trip)
    fun drawTripUsersLocation(tripUid: String, currentUser: User? = null)
    fun drawMarker(position: LatLng)
    fun clearLastRoad()

}