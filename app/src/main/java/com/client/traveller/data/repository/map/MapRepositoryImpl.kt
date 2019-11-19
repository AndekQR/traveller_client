package com.client.traveller.data.repository.map

import android.content.Context
import android.os.Bundle
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.network.map.MapUtils
import com.client.traveller.data.network.api.directions.model.TravelMode
import com.client.traveller.data.network.api.directions.response.Distance
import com.client.traveller.data.network.api.geocoding.GeocodingApiService
import com.client.traveller.data.network.api.geocoding.response.geocodingResponse.Location
import com.client.traveller.data.provider.LocationProvider
import com.client.traveller.ui.util.Coroutines.io

import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng


class MapRepositoryImpl(
    private val mapUtils: MapUtils,
    private val locationProvider: LocationProvider,
    private val geocoding: GeocodingApiService
) : MapRepository {

    /**
     * Inicjalzacja mapy oraz jej składników
     * [mapUtils] jest inicjalizowany w callbacku bo musimy zaczekać na [locationProvider]
     */
    override fun initializeMap(
        mapFragment: SupportMapFragment,
        context: Context,
        savedInstanceState: Bundle?
    ) {
        locationProvider.init(mapFragment, context, savedInstanceState) {
            mapUtils.initializeMap(context)
        }
    }

    override fun startLocationUpdates() = locationProvider.startLocationUpdates()
    override fun stopLocationUpdates() = locationProvider.stopLocationUpdates()
    override fun sendingLocationData() = locationProvider.sendingLocationData()
    override fun centerCurrentLocation() = mapUtils.centerCurrentLocation()

    override suspend fun getDistance(
        origin: String,
        destination: String,
        waypoints: ArrayList<String>?,
        mode: TravelMode
    ): Distance? {
        return mapUtils.getDistance(origin, destination, waypoints)
    }

    override fun drawRouteMarker() = mapUtils.drawRouteToMarker()
    override fun drawRouteToLocation(
        origin: String,
        destination: String,
        locations: List<String>,
        mode: TravelMode
    ) {
        mapUtils.drawRouteToLocation(origin, destination, locations, mode)
    }

    override fun getCurrentLocation() = mapUtils.getCurrentLocation()
    override fun clearMap() = mapUtils.clearMap()

    override suspend fun drawTripRoute(trip: Trip) {
        val startAddress = geocoding.geocode(trip.startAddress!!)
        mapUtils.drawMarkerWithText(startAddress.results.first().geometry.location.toLatLng(), "START")
    }

    private fun Location.toLatLng(): LatLng {
        return LatLng(this.lat, this.lng)
    }
}