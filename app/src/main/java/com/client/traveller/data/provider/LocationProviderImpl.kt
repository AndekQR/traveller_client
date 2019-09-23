package com.client.traveller.data.provider

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.text.DateFormat
import java.util.*

/**
 * Klasa zarządzająca lokalizacją urządzenia
 *
 * @param fusedLocationClient: zapewnia API do lokalizacji. Wstrzykiwany przez KODEIN
 * @param preferenceProvider: obiekt klasy [PreferenceProvider], zapewnia dostęp do stanu ustawień aplikacji. Wstrzykiwany przez KODEIN
 */
class LocationProviderImpl(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val preferenceProvider: PreferenceProvider
) : LocationProvider {


    /**
     * [TAG] używany do logowania
     * [SEND_LOCATION] i [CAMERA_TRACKING] stałe które mają przypisane klucze pól w ustawieniach aplikacji
     */
    val TAG = this::class.java.simpleName
    private val SEND_LOCATION = "SEND_LOCATION"
    private val CAMERA_TRACKING = "CAMERA_TRACKING"

    private var mMap: GoogleMap? = null

    var lastUpdateTime: String? = null
    private val updateIntervalMs: Long = 5000
    private val fastestUpdateIntervalMs: Long = 3000
    private val requestCheckSettings = 100

    private var settingsClient: SettingsClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationSettingsRequest: LocationSettingsRequest? = null
    private var locationCallback: LocationCallback? = null
    var currentLocation: Location? = null

    private var requestingLocationUpdates: Boolean = false

    private lateinit var context: Context

    /**
     * Metoda inicjalizująca jest wywoływana za każdym razem kiedy jest tworzona [HomeActivity]
     * cała wykonuje się tylko raz, potem jest zawsze zatrzymywana w if przy sprawdzaniu [mMap]
     *
     * @param mapFragment: inicjalizowany w [HomeActivity], ponieważ mapa należy właśnie do tej aktywności
     * @param context: context [HomeActivity]
     * @param savedInstanceState: dane dotyczące lokalizacji, zapisane przed znisczeniem aktywności
     */
    override fun init(
        mapFragment: SupportMapFragment,
        context: Context,
        savedInstanceState: Bundle?
    ) {

        this.restoreValuesFromBundle(savedInstanceState)

        if (mMap != null) {
            mapFragment.getMapAsync(this)
            return
        }

        this.context = context

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                locationResult ?: return

                currentLocation = locationResult.lastLocation
                lastUpdateTime = DateFormat.getTimeInstance().format(Date())
                updateLocationUI()

            }
        }
        locationRequest = LocationRequest.create()?.apply {
            interval = updateIntervalMs
            fastestInterval = fastestUpdateIntervalMs
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)

        settingsClient = LocationServices.getSettingsClient(context)
        locationSettingsRequest = builder.build()

        mapFragment.getMapAsync(this)  // This class automatically initializes the maps system and the view.
    }

    /**
     * Wywoływana zawsze po .getMapAsync()
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap == null)
            return

        mMap = googleMap
        checkPermissions()
        mMap?.isMyLocationEnabled = true //blue dot on the map
        this.changeMapUI()
        mMap?.setOnMapClickListener(this)
        startLocationUpdates()

    }

    /**
     * isBuildingEnabled = włącza wyświetlanie budynków 3D
     *
     */
    private fun changeMapUI() {
        val ui = mMap?.uiSettings

        ui?.isMyLocationButtonEnabled = false
        ui?.isCompassEnabled = false
        mMap?.isBuildingsEnabled = true
    }

    //TODO w HomeActivity nie ma zapisywania tych wartości -> nie ma co przywracać
    private fun restoreValuesFromBundle(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("is_requesting_updates")) {
                requestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates")
            }

            if (savedInstanceState.containsKey("last_known_location")) {
                currentLocation = savedInstanceState.getParcelable("last_known_location")
            }

            if (savedInstanceState.containsKey("last_updated_on")) {
                lastUpdateTime = savedInstanceState.getString("last_updated_on")
            }
        }

        updateLocationUI()
    }

    fun updateLocationUI() {
        if (currentLocation == null)
            return

        // location last updated time
        //txtUpdatedOn.setText("Last updated on: " + mLastUpdateTime);
        Toast.makeText(
            context,
            "Lat: " + currentLocation?.latitude + ", Lng: " + currentLocation?.longitude,
            Toast.LENGTH_LONG
        ).show()

        if (preferenceProvider.getPreferenceState(CAMERA_TRACKING)) {
            var cameraPosition = CameraPosition.Builder().zoom(17F).tilt(50F).target(
                LatLng(
                    currentLocation?.latitude!!,
                    currentLocation?.longitude!!
                )
            ).build()

            mMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 3000, null)
        }
    }


    @SuppressLint("MissingPermission")
    override fun startLocationUpdates() {
        if (!preferenceProvider.getPreferenceState(SEND_LOCATION))
            return

        checkPermissions()
        requestingLocationUpdates = true
        settingsClient?.checkLocationSettings(locationSettingsRequest)
            ?.addOnSuccessListener(context as Activity) {

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback, Looper.myLooper()
                )

                updateLocationUI()
            }
            ?.addOnFailureListener(context as Activity) { e ->
                when ((e as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(context as Activity, requestCheckSettings)
                        } catch (sie: IntentSender.SendIntentException) {
                            Log.i(TAG, "PendingIntent unable to execute request.")
                        }

                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        val errorMessage =
                            "Location settings are inadequate, and cannot be " + "fixed here. Fix in Settings."
                        Log.e(TAG, errorMessage)

                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
                updateLocationUI()
            }
    }

    override fun stopLocationUpdates() {
        requestingLocationUpdates = false

        fusedLocationClient.removeLocationUpdates(locationCallback)
            ?.addOnCompleteListener(context as Activity) {
                Toast.makeText(context, "Location updates stopped!", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Android Studio nie wykrywa sprawdzania uprawnień w osobnej metodzie
     * w rezultacie przy niektórych metodach jest adnotacja @SuppressLint("MissingPermission")
     */
    override fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                121
            )
        }
    }

    override fun sendingLocationData(): Boolean {
        if (!requestingLocationUpdates && preferenceProvider.getPreferenceState(SEND_LOCATION)) {
            return true
        } else if (requestingLocationUpdates && !preferenceProvider.getPreferenceState(SEND_LOCATION)) {
            return false
        } else if (requestingLocationUpdates && preferenceProvider.getPreferenceState(SEND_LOCATION)) {
            return true
        } else if (!requestingLocationUpdates && !preferenceProvider.getPreferenceState(
                SEND_LOCATION
            )
        ) {
            return false
        }
        return false
    }

    /**
     * Jest zawsze wywoływana przy zmianie nasłuchiwanego ustawienia w ustawieniach aplikacji
     * Słuchacz jest inicjalizowany w pliku [SettingsActivity]
     *
     *
     * W przypadku [newValue]==true nie ma potrzeby wywoływać [startLocationUpdates] ponieważ jest wywołana w [onRosume] w [HomeActivity]
     */
    override fun onPreferenceChange(
        preference: androidx.preference.Preference?,
        newValue: Any?
    ): Boolean {
        if (newValue != null && preference?.key == SEND_LOCATION) {
            if (newValue as Boolean)
                requestingLocationUpdates = true
            else
                stopLocationUpdates()
        }
        return true
    }


    override fun onMapClick(position: LatLng) {
        // TODO do zaimplementowania, po kliknięci wyskakuje menu z tym miejscem i z informacjami o nim, jeżeli nie ma w danym miejscu nic to obiekty w pobliżu
        // znacznik czyszczony po po otwrciu menu
        // mmenu się
        mMap?.addMarker(MarkerOptions().position(position).title("onClick"))
    }

    fun showLastKnownLocation() {
        Toast.makeText(
            context, "Lat: " + currentLocation?.latitude
                    + ", Lng: " + currentLocation?.longitude, Toast.LENGTH_LONG
        ).show()
        // Add a marker and move the camera
        try {
            val latlng = LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!)
            mMap?.addMarker(MarkerOptions().position(latlng).title("Last location"))
            mMap?.moveCamera(CameraUpdateFactory.newLatLng(latlng))
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 12.0f))
        } catch (ex: NullPointerException) {
            Toast.makeText(
                context,
                "The current location's latitude or longitude is not present!",
                Toast.LENGTH_LONG
            )
                .show()
        }

    }


}