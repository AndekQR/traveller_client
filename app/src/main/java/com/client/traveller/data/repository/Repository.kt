package com.client.traveller.data.repository

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import com.client.traveller.data.db.entities.User
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.auth.FirebaseUser

interface Repository {
    suspend fun saveUser(firebaseUser: FirebaseUser)
    fun getUser(): LiveData<User>
    suspend fun deleteUser() //jest tylko jeden aktualnie zalogowany!
    fun initLocationProvider(map: SupportMapFragment, context: Context, savedInstanceState: Bundle?)
    fun startLocationUpdates()
    fun stopLocationUpdates()
    fun sendingLocationData(): Boolean
    fun requestLocationPermission()
}