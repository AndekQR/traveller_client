package com.client.traveller.data.repository.map

import android.content.Context
import android.os.Bundle
import com.client.traveller.data.network.map.MapUtils
import com.client.traveller.data.network.map.directions.model.TravelMode
import com.client.traveller.data.network.map.directions.response.Distance
import com.client.traveller.data.provider.LocationProvider
import com.google.android.gms.maps.SupportMapFragment


class MapRepositoryImpl(
    private val mapUtils: MapUtils,
    private val locationProvider: LocationProvider
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
}