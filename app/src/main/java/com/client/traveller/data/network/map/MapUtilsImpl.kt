package com.client.traveller.data.network.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.util.Log
import com.client.traveller.R
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.api.directions.DirectionsApiService
import com.client.traveller.data.network.api.directions.model.TravelMode
import com.client.traveller.data.network.api.directions.response.Directions
import com.client.traveller.data.network.api.directions.response.Distance
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
import com.client.traveller.data.network.firebase.firestore.Users
import com.client.traveller.data.network.firebase.firestore.model.UserLocalization
import com.client.traveller.ui.util.format
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.google.maps.android.clustering.ClusterManager

/**
 * Klasa do zarządzania mapą w [HomeFragment]
 * @param locationProvider wstrzykiwany przez kodein, używany do pobrania mapy i jej widoku
 * @param directionsApiService wyszukiwanie miejsc
 */
class MapUtilsImpl(
    private val directionsApiService: DirectionsApiService
) : MapUtils {

    private lateinit var context: Context
    private lateinit var nearbyPlacesClusterManager: ClusterManager<NearbyPlaceClusterItem>
    private lateinit var usersPositionClusterManager: ClusterManager<UserLocationClusterItem>
    private var mMap: GoogleMap? = null

    private var mainMarker: Marker? = null
    private val polylinesOnMap = mutableListOf<Polyline>()
    private val markers = mutableListOf<Marker>()
    private val nearbyPlacesClusterItems = mutableListOf<NearbyPlaceClusterItem>()
    private val usersPositionClusterItems = mutableListOf<UserLocationClusterItem>()

    /**
     * isBuildingEnabled = włącza wyświetlanie budynków 3D
     *
     */
    override fun initializeMap(
        context: Context,
        mapFragment: SupportMapFragment
    ) {
        this.context = context
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.mMap = googleMap
        this.mMap?.isMyLocationEnabled = true
        this.mMap?.setOnMapClickListener(this)
        this.mMap?.setOnMapLongClickListener(this)

        val ui = this.mMap?.uiSettings
        ui?.isMyLocationButtonEnabled = false
        ui?.isCompassEnabled = false
        ui?.isMapToolbarEnabled = false
        this.mMap?.isBuildingsEnabled = true

        this.setupClasterer()
    }

    private fun setupClasterer() {
        //nearby cluster manager
        this.nearbyPlacesClusterManager = ClusterManager(this.context, this.mMap)
        this.nearbyPlacesClusterManager.renderer = MyClusterItemRenderer(this.context,
            this.mMap!!, this.nearbyPlacesClusterManager)

        // user location cluster manager
        this.usersPositionClusterManager = ClusterManager(this.context, this.mMap)
        this.usersPositionClusterManager.renderer = MyClusterItemRenderer(this.context, this.mMap!!, this.usersPositionClusterManager)

        this.mMap?.setOnCameraIdleListener {
            this.usersPositionClusterManager.onCameraIdle()
            this.nearbyPlacesClusterManager.onCameraIdle()
        }
        this.mMap?.setOnMarkerClickListener {
            this.usersPositionClusterManager.onMarkerClick(it)
            this.nearbyPlacesClusterManager.onMarkerClick(it)
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        marker?.remove()
        return true
    }

    override fun getMarkerFromMap(): Marker? {
        return this.mainMarker
    }

    /**
     * @param toClear jeżeli true to marker jest dodawana do globalnej tablicy, dzięki czemu możemy go wyczyścić z mapy
     */
    override fun drawMarkerFromBitmap(position: LatLng, bitmap: Bitmap, toClear: Boolean) {
        val markerOptions = MarkerOptions()
            .position(position)
            .draggable(false)
            .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        val marker = this.mMap?.addMarker(markerOptions)
        if (toClear && marker != null) this.markers.add(marker)
    }

    override fun drawPlaceMarkersInCluster(places: Set<Result>) {
        places.forEach {
            val item = NearbyPlaceClusterItem(it)
            this.nearbyPlacesClusterItems.add(item)
            this.nearbyPlacesClusterManager.addItem(item)
        }
        this.nearbyPlacesClusterManager.cluster()
    }

    override fun drawMainMarker(position: LatLng): Marker? {
        val defaultMarker = MarkerOptions()
            .position(position)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.maps_and_flags))
            .draggable(false)

          return this.mMap?.addMarker(defaultMarker)

    }

    override fun onMapClick(position: LatLng) {
        // TODO do zaimplementowania, po kliknięci wyskakuje menu z tym miejscem i z informacjami o nim, jeżeli nie ma w danym miejscu nic to obiekty w pobliżu
        // znacznik czyszczony po po otwrciu menu
        // mmenu się
//        markerOnMap?.remove()
//        this.drawMarker(position)
    }

    override fun onMapLongClick(position: LatLng) {
        val marker = this.drawMainMarker(position)
        marker?.let {
            this.mainMarker?.remove()
            this.mainMarker = it
        }
    }

    private fun getDefaultPolyline(): PolylineOptions {
        return PolylineOptions()
            .width(12F)
            .color(Color.rgb(124, 77, 255))
            .geodesic(true)
    }

    override suspend fun drawRouteToLocation(
        origin: String?,
        destination: String?,
        locations: List<String>?,
        mode: TravelMode,
        clearAble: Boolean
    ) {
        if (origin == null || destination == null) return
        val start = origin.trim().replace(" ", "+")
        val stop = destination.trim().replace(" ", "+")
        val waypoints = this.getWaypointsString(waypoints = locations)

        val result = if (waypoints == null) this.directionsApiService.getDirections(start, stop, mode.name)
        else directionsApiService.getDirectionsWithWaypoints(start, stop, mode.name, waypoints)
        val polyline =  this.drawRoute(result)
        if (clearAble) polyline?.let { this.polylinesOnMap.add(it) }
    }

    private fun getWaypointsString(waypoints: List<String>?): String? {
        return waypoints?.map { it.trim() }?.joinToString("|")
    }

    override suspend fun drawRouteToMarker(location: Location, marker: Marker?): Polyline? {
        val origin: String
        val destination: String

        try {
            origin = location.format()
            destination = marker?.position?.format()!!

            val result = directionsApiService.getDirections(origin, destination)
            return this.drawRoute(result)

        } catch (ex: Exception) {
            Log.e(javaClass.simpleName, ex.message)
        }
        return null
    }

    private fun drawRoute(result: Directions): Polyline? {
        if (result.status == "OK") {
            val lineOptions = this.getDefaultPolyline()
            val pointList = PolyUtil.decode(result.routes.first().overviewPolyline.points)
            pointList.forEach {
                lineOptions.add(it)
            }
            return this.mMap?.addPolyline(lineOptions)
        }
        return null
    }

    override fun clearMap() {
        this.mainMarker?.remove()
        this.polylinesOnMap.forEach {
            it.remove()
        }
        this.markers.forEach {
            it.remove()
        }
        this.nearbyPlacesClusterItems.forEach {
            this.nearbyPlacesClusterManager.removeItem(it)
        }
        this.usersPositionClusterItems.forEach {
            this.usersPositionClusterManager.removeItem(it)
        }
        this.markers.clear()
        this.nearbyPlacesClusterItems.clear()
        this.polylinesOnMap.clear()
        this.usersPositionClusterItems.clear()
    }

    override fun centerCameraOnRoute(
        startAddress: LatLng,
        waypoints: ArrayList<LatLng?>?,
        endAddress: LatLng
    ) {
        val latLngBoundsBuilder = LatLngBounds.Builder()
        latLngBoundsBuilder.include(startAddress)
        waypoints?.forEach {
            it?.let { latLngBoundsBuilder.include(it) }
        }
        latLngBoundsBuilder.include(endAddress)
        val bounds = latLngBoundsBuilder.build()
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 170)
        this.mMap?.animateCamera(cameraUpdate)
    }

    override fun centerCameraOnLocation(location: LatLng) {
        val cameraPosition = CameraPosition.Builder()
            .target(location)
            .zoom(17F)
            .tilt(50F)
            .build()
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
        this.mMap?.animateCamera(cameraUpdate)
    }

    /**
     * Zwraca [Distance] trasy podanej w parametrach
     */
    override suspend fun getDistance(
        origin: String,
        destination: String,
        waypoints: ArrayList<String>?
    ): Distance? {
        var result: Directions? = null
        when {
            waypoints != null -> result = directionsApiService.getDirectionsWithWaypoints(
                origin,
                destination,
                TravelMode.driving.name,
                this.getWaypointsString(waypoints)!!
            )
            waypoints == null -> result =
                directionsApiService.getDirections(origin, destination, TravelMode.driving.name)
        }
        return if (result != null && result.status == "OK") {
            result.routes.first().legs.first().distance
        } else
            null
    }

    /**
     * sprawdza czy na mappie znajdują się elementy które można wyczyścić
     */
    override fun elementsOnMap(): Boolean {
        if (this.mainMarker != null) return true
        if (this.polylinesOnMap.isNotEmpty()) return true
        return false
    }

    override fun disableMapDragging() {
        this.mMap?.uiSettings?.isScrollGesturesEnabled = false
    }

    override fun enableMapDragging() {
        this.mMap?.uiSettings?.isScrollGesturesEnabled = true
    }

    override fun updateUserPositionMarker(
        data: UserLocalization,
        currentUser: User?
    ) {
        // nie rysujemy markera dla aktualnego zalogowanego użytkownika
        if (data.userUidFirebase == currentUser?.idUserFirebase) return
        Users.getUserByUid(data.userUidFirebase!!).addOnSuccessListener { documentSnapshot ->
            val user = documentSnapshot.toObject(User::class.java)
            user?.let {
                val latlng = LatLng(data.latlng?.latitude!!, data.latlng?.longitude!!)
                this.drawUsersLocationClusterItem(latlng, it)
            }
        }
    }

    private fun drawUsersLocationClusterItem(latLng: LatLng, user: User) {
        val item = UserLocationClusterItem(latLng, user)
        this.removeLastMarker(user)
        this.usersPositionClusterItems.add(item)
        this.usersPositionClusterManager.addItem(item)
        this.usersPositionClusterManager.cluster()
    }

    private fun removeLastMarker(user: User) {
        val lastItem = this.usersPositionClusterItems.find { userLocationClusterItem ->
            userLocationClusterItem.user.idUserFirebase == user.idUserFirebase
        }
        lastItem?.let {
            this.usersPositionClusterItems.remove(it)
            this.usersPositionClusterManager.removeItem(it)
        }
    }

}
