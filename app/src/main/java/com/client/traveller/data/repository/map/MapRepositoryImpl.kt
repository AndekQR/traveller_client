package com.client.traveller.data.repository.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.client.traveller.R
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.api.directions.model.TravelMode
import com.client.traveller.data.network.api.directions.response.Distance
import com.client.traveller.data.network.api.geocoding.GeocodingApiService
import com.client.traveller.data.network.api.geocoding.response.geocodingResponse.Location
import com.client.traveller.data.network.api.places.API_KEY
import com.client.traveller.data.network.api.places.PlacesApiService
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
import com.client.traveller.data.network.firebase.firestore.Map
import com.client.traveller.data.network.firebase.firestore.model.UserLocalization
import com.client.traveller.data.network.map.MapUtils
import com.client.traveller.ui.util.formatToApi
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.my_simple_marker_view.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi


class MapRepositoryImpl(
    private val mapUtils: MapUtils,
    private val geocoding: GeocodingApiService
) : MapRepository {

    private lateinit var context: Context
    private var tripUsersLocationListener: ListenerRegistration? = null

    /**
     * Inicjalzacja mapy oraz jej składników
     * [mapUtils] jest inicjalizowany w callbacku bo musimy zaczekać na [locationProvider]
     */
    override fun initializeMap(
        mapFragment: SupportMapFragment,
        context: Context,
        savedInstanceState: Bundle?,
        locationToCenter: LatLng?
    ) {
        this.context = context
        mapUtils.initMainMap(context, mapFragment, locationToCenter)
    }

    override suspend fun getDistance(
        origin: String,
        destination: String,
        waypoints: ArrayList<String>?,
        mode: TravelMode
    ): Distance? {
        return mapUtils.getDistance(origin, destination, waypoints)
    }

    override suspend fun drawRouteToMainMarker(location: android.location.Location) =
        this.mapUtils.drawRouteToMarker(location, this.mapUtils.getMarkerFromMap())

    override suspend fun drawRouteToLocation(
        origin: String,
        destination: String,
        locations: List<String>?,
        mode: TravelMode
    ) {
        mapUtils.drawRouteToLocation(origin, destination, locations, mode, randomColor = true)
    }


    override fun clearMap() = this.mapUtils.clearMap()

    override suspend fun drawTripRoute(trip: Trip, travelMode: TravelMode) {
        var startLatLng: String? = null
        val waypointsLatLng = mutableListOf<String>()
        var endLatLng: String? = null

        val view =
            (this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                R.layout.my_simple_marker_view, null
            )
        //start
        view.marker_text.text = "S"
        var bitmap = this.getBitmapFromView(view)
        var address = geocoding.geocode(trip.startAddress!!)
        if (address.results.isNotEmpty()) {
            val latlng = address.results.first().geometry.location.toLatLng()
            startLatLng = latlng.formatToApi()
            bitmap?.let {
                mapUtils.drawMarkerFromBitmap(
                    latlng,
                    it
                )
            }
        }

        //waypoints
        trip.waypoints?.forEachIndexed { index, waypointAddress ->
            view.marker_text.text = "${index + 1}"
            val address = geocoding.geocode(waypointAddress)
            if (address.results.isNotEmpty()) {
                val latlng = address.results.first().geometry.location.toLatLng()
                waypointsLatLng.add(latlng.formatToApi())
                val bitmap = this.getBitmapFromView(view)
                bitmap?.let { this.mapUtils.drawMarkerFromBitmap(latlng, bitmap) }
            }
        }

        //stop
        if (trip.startAddress?.trim() == trip.endAddress?.trim())
            view.marker_text.text = "S-E"
        else {
            view.marker_text.text = "E"
        }
        address = geocoding.geocode(trip.endAddress!!)
        if (address.results.isNotEmpty()) {
            val latlng = address.results.first().geometry.location.toLatLng()
            endLatLng = latlng.formatToApi()
            bitmap = this.getBitmapFromView(view)
            bitmap?.let { this.mapUtils.drawMarkerFromBitmap(latlng, it) }
        }

        this.mapUtils.drawRouteToLocation(
            startLatLng,
            endLatLng,
            waypointsLatLng,
            travelMode,
            false
        )
    }

    override suspend fun drawNearbyPlaceMarkers(places: Set<Result>) {
        this.mapUtils.drawPlaceMarkersInCluster(places)
    }

    override fun getPhotoUrl(reference: String, width: Int): String {
        return "${PlacesApiService.BASE_URL}photo?maxwidth=${width}&photoreference=${reference}&key=$API_KEY"
    }

    private fun Location.toLatLng(): LatLng {
        return LatLng(this.lat, this.lng)
    }

    private fun getBitmapFromView(view: View): Bitmap? {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap =
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)
        view.background?.draw(canvas)
        view.draw(canvas)
        return bitmap
    }

    override fun centerCameraOnLocation(location: LatLng, putMarker: Boolean) {
        this.mapUtils.centerCameraOnLocation(location)
        if (putMarker) this.mapUtils.drawMainMarker(location)
    }

    override suspend fun centerCameraOnRoute(
        startAddress: String,
        waypoints: ArrayList<String>?,
        endAddress: String
    ) {
        val startAddressLatLng = this.getLatLng(startAddress)
        val waypointsLatLng = waypoints?.map {
            this.getLatLng(it)
        }?.toCollection(ArrayList())
        val endAddressLatLng = this.getLatLng(endAddress)
        if (startAddressLatLng == null || endAddressLatLng == null) return
        this.mapUtils.centerCameraOnRoute(startAddressLatLng, waypointsLatLng, endAddressLatLng)
    }

    private suspend fun getLatLng(address: String): LatLng? {
        val result = this.geocoding.geocode(address).results
        return if (result.isNotEmpty()) {
            result.first().geometry.location.toLatLng()
        } else {
            null
        }
    }

    override fun elementOnMap() = this.mapUtils.elementsOnMap()
    override fun getActualMarker() = this.mapUtils.getMarkerFromMap()
    override fun disableMapDragging() = this.mapUtils.disableMapDragging()
    override suspend fun geocodeAddress(address: String) = this.geocoding.geocode(address)
    override suspend fun reverseGeocoding(latlng: String) = this.geocoding.reverseGeocode(latlng)

    override fun sendNewLocation(userLocalization: UserLocalization, trip: Trip) {

    }

    /**
     * rysuje na mapie markery osób z tej samej wycieczki
     * @return obiekty typu [UserLocalization] w flow
     */
    @ExperimentalCoroutinesApi
    override fun drawTripUsersLocation(tripUid: String, currentUser: User?) {
        if (this.tripUsersLocationListener != null) {
            this.tripUsersLocationListener?.remove()
        }
        Log.e(javaClass.simpleName, tripUid)
        this.tripUsersLocationListener =
            Map.getUserLocalizationCollection(tripUid)
                .addSnapshotListener { querySnapshot, exception ->
                    exception?.let {
                        return@addSnapshotListener
                    }
                    querySnapshot?.let { snapshot ->
                        for (dc in snapshot.documentChanges) {
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    // wywoływane na początku, inicjalizacja bo na początku cache zapytania jest pusty
                                    val data = dc.document.toObject(UserLocalization::class.java)
                                    Log.e(javaClass.simpleName, "ADDED")
                                    Log.e(javaClass.simpleName, data.userUidFirebase)
                                    this@MapRepositoryImpl.mapUtils.updateUserPositionMarker(data, currentUser)

                                }
                                DocumentChange.Type.MODIFIED -> {
                                    val data = dc.document.toObject(UserLocalization::class.java)
                                    Log.e(javaClass.simpleName, "MODIFIED")
                                    Log.e(javaClass.simpleName, data.userUidFirebase)
                                    this@MapRepositoryImpl.mapUtils.updateUserPositionMarker(data, currentUser)
                                }
                                DocumentChange.Type.REMOVED -> {
                                    Log.i(javaClass.simpleName, "REMOVED")
                                }
                            }
                        }
                    }
                }
    }

    override fun drawMarker(position: LatLng) = this.mapUtils.drawMarker(position)
    override fun clearLastRoad() = this.mapUtils.clearLastRoad()

}