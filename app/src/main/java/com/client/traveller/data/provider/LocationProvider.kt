package com.client.traveller.data.provider

import android.content.Context
import android.location.Location
import android.os.Bundle
import androidx.preference.Preference

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

interface LocationProvider : OnMapReadyCallback,
    Preference.OnPreferenceChangeListener {

    /**
     * Pola w interfejsie są umieszczone jako val w rezultacie czego inne klasy nie mogą ich nadpisać
     * W klasie implementującej ten interfejs są zaimplementowane jako var
     */
    val isInitialized: Boolean
    val currentLocation: Location?
    val lastUpdateTime: String?
    val mMap: GoogleMap?
    val mapFragment: SupportMapFragment?

    fun init(mapFragment: SupportMapFragment, context: Context, savedInstanceState: Bundle?, function: () -> Unit)
    fun startLocationUpdates()
    fun stopLocationUpdates()
    fun sendingLocationData(): Boolean
    fun checkPermissions()

}