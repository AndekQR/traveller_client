package com.client.traveller.ui.home

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.provider.LocationProvider
import com.client.traveller.data.repository.Repository
import com.client.traveller.ui.util.Coroutines
import com.google.android.gms.maps.SupportMapFragment

class HomeViewModel(
    private val repository: Repository,
    private val locationProvider: LocationProvider
): ViewModel() {


    fun getLoggedInUser(): LiveData<User> {
        return repository.getUser()
    }

    fun logoutUser(){
        Coroutines.io {
            repository.deleteUser()
        }
    }

    fun initLocationProvider(map: SupportMapFragment, context: Context, savedInstanceState: Bundle?){
        return locationProvider.init(map, context, savedInstanceState)
    }

    fun startLocationUpdates(){
        return locationProvider.startLocationUpdates()
    }

    fun stopLocationUpdates(){
        return locationProvider.stopLocationUpdates()
    }

    fun sendingLocationData(): Boolean{
        return locationProvider.sendingLocationData()
    }

}