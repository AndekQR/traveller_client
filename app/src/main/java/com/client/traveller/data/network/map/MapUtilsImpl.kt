package com.client.traveller.data.network.map

import android.util.Log
import android.widget.Toast
import com.client.traveller.data.provider.LocationProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapUtilsImpl(private val locationProvider: LocationProvider) : MapUtils {

    private var map: GoogleMap? = null

    init {
        map = locationProvider.getMap()
        Log.e(javaClass.simpleName, "map: "+map.toString())
        map?.setOnMapClickListener(this)
        //TODO można zrobić callback gdy mapa bedzie nullem na początku
        // zostanie on wywołany gdy w locationprovider już nie będzie nullem
    }

    override fun onMapClick(position: LatLng) {
        // TODO do zaimplementowania, po kliknięci wyskakuje menu z tym miejscem i z informacjami o nim, jeżeli nie ma w danym miejscu nic to obiekty w pobliżu
        // znacznik czyszczony po po otwrciu menu
        // mmenu się
        map?.addMarker(MarkerOptions().position(position).title("onClick"))
    }

    override fun lastKnownLocation() {
        try {
            val latlng = LatLng(locationProvider.currentLocation?.latitude!!, locationProvider.currentLocation?.longitude!!)
            map?.addMarker(MarkerOptions().position(latlng).title("Last location"))
            map?.moveCamera(CameraUpdateFactory.newLatLng(latlng))
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 12.0f))
        } catch (ex: NullPointerException) {
            Log.e(javaClass.simpleName, ex.message)
        }

    }
}