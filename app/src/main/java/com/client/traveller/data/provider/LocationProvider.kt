package com.client.traveller.data.provider

import android.content.Context
import android.os.Bundle
import androidx.preference.Preference

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

interface LocationProvider : OnMapReadyCallback, GoogleMap.OnMapClickListener, Preference.OnPreferenceChangeListener {

    fun init(mapFragment: SupportMapFragment, context: Context, savedInstanceState: Bundle?)
    fun startLocationUpdates()
    fun stopLocationUpdates()
    fun sendingLocationData(): Boolean
    fun checkPermissions()
}