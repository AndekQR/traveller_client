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

    var currentLocation: Location?
    var lastUpdateTime: String?

    fun init(mapFragment: SupportMapFragment, context: Context, savedInstanceState: Bundle?)
    fun startLocationUpdates()
    fun stopLocationUpdates()
    fun sendingLocationData(): Boolean
    fun checkPermissions()
    fun getMap(): GoogleMap?
}