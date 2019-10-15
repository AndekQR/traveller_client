package com.client.traveller.data.repository.map

import android.content.Context
import android.os.Bundle
import com.client.traveller.data.network.map.MapUtils
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
    override fun initializeMap(mapFragment: SupportMapFragment, context: Context, savedInstanceState: Bundle?) {
        locationProvider.init(mapFragment, context, savedInstanceState){
            mapUtils.initializeMap(context)
        }
    }

    override fun startLocationUpdates() = locationProvider.startLocationUpdates()
    override fun stopLocationUpdates() = locationProvider.stopLocationUpdates()
    override fun sendingLocationData() =  locationProvider.sendingLocationData()
    override fun centerCurrentLocation() = mapUtils.centerCurrentLocation()

}