package com.client.traveller.data.network.map

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import com.client.traveller.R
import com.client.traveller.data.network.map.directions.DirectionsApiService
import com.client.traveller.data.network.map.directions.model.Route
import com.client.traveller.data.network.map.directions.model.TravelMode
import com.client.traveller.data.network.map.directions.response.Directions
import com.client.traveller.data.provider.LocationProvider
import com.client.traveller.ui.util.Coroutines
import com.client.traveller.ui.util.Coroutines.main
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil

/**
 * Klasa do zarządzania mapą w [HomeFragment]
 * @param locationProvider wstrzykiwany przez kodein, używany do pobrania mapy i jej widoku
 */
class MapUtilsImpl(
    private val locationProvider: LocationProvider,
    private val directionsApiService: DirectionsApiService
) : MapUtils {

    private var markerOnMap: Marker? = null
    private lateinit var context: Context

    /**
     * isBuildingEnabled = włącza wyświetlanie budynków 3D
     *
     */
    override fun initializeMap(context: Context) {
        this.context = context
        locationProvider.mMap?.setOnMapClickListener(this)
        locationProvider.mMap?.setOnMapLongClickListener(this)

        val ui = locationProvider.mMap?.uiSettings
        ui?.isMyLocationButtonEnabled = true
        ui?.isCompassEnabled = false
        ui?.isMapToolbarEnabled = false
        locationProvider.mMap?.isBuildingsEnabled = true

        // zmiana lokalizacji przycisku do centrowania lokalizacji na prawy dół
        val locationButton =
            (locationProvider.mapFragment?.view?.findViewById<View>(Integer.parseInt("1"))?.parent as View).findViewById<View>(
                Integer.parseInt("2")
            )
        val rlp = locationButton.layoutParams as (RelativeLayout.LayoutParams)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rlp.setMargins(0, 0, 30, 30)

    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        marker?.remove()
        return true
    }

    private fun drawMarker(position: LatLng, default: Boolean = true) {
        val defaultMarker = MarkerOptions()
            .position(position)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.maps_and_flags))
            .draggable(false)

        if (default)
            this.markerOnMap = locationProvider.mMap?.addMarker(defaultMarker)
    }

    override fun onMapClick(position: LatLng) {
        // TODO do zaimplementowania, po kliknięci wyskakuje menu z tym miejscem i z informacjami o nim, jeżeli nie ma w danym miejscu nic to obiekty w pobliżu
        // znacznik czyszczony po po otwrciu menu
        // mmenu się
        markerOnMap?.remove()
        this.drawMarker(position)
    }

    override fun onMapLongClick(position: LatLng) {
        locationProvider.mMap?.clear()
        this.drawMarker(position)
        this.drawRouteToMarker()
    }

    override fun lastKnownLocation() {
        try {
            val latlng = LatLng(
                locationProvider.currentLocation?.latitude!!,
                locationProvider.currentLocation?.longitude!!
            )
            locationProvider.mMap?.addMarker(MarkerOptions().position(latlng).title("Last location"))
            locationProvider.mMap?.moveCamera(CameraUpdateFactory.newLatLng(latlng))
            locationProvider.mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 12.0f))
        } catch (ex: NullPointerException) {
            Log.e(javaClass.simpleName, ex.message)
        }
    }

    private fun getLocationString(location: Location?): String{
        return "${location?.latitude},${location?.longitude}"
    }
    private fun getPositionString(position: LatLng?): String {
        return "${position?.latitude},${position?.longitude}"
    }
    private fun getDefaultPolyline(): PolylineOptions {
        return PolylineOptions()
            .width(12F)
            .color(Color.rgb(124,77,255))
            .geodesic(true)
    }

    override fun drawRouteToLocation(origin: String, destination: String, locations: List<String>, mode: TravelMode) {
        val start = origin.trim().replace(" ", "+")
        val stop = destination.trim().replace(" ", "+")
        val waypoints = locations.map { it.trim().replace(" ", "+") }

        main {
            val result = directionsApiService.getDirectionsWithWaypoints(start, stop, mode.name, waypoints)
            this.drawRoute(result)
        }
    }

    override fun drawRouteToMarker() {
        if (locationProvider.currentLocation == null)
            return

        val origin: String
        val destination: String

        try {
            origin = this.getLocationString(locationProvider.currentLocation)
            destination = this.getPositionString(this.markerOnMap?.position)

            main {
                val result = directionsApiService.getDirections(origin, destination)
                this.drawRoute(result)
            }
        } catch (ex: Exception) {
            Log.e(javaClass.simpleName, ex.message)
        }
    }

    private fun drawRoute(result: Directions)  {
        if (result.status == "OK") {
            val lineOptions = this.getDefaultPolyline()
            val pointList = PolyUtil.decode(result.routes.first().overviewPolyline.points)
            pointList.forEach {
                lineOptions.add(it)
            }
            locationProvider.mMap?.addPolyline(lineOptions)
        }
    }
}
