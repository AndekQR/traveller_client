package com.client.traveller.data.repository

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import com.client.traveller.data.db.UserDao
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.SafeApiRequest
import com.client.traveller.data.network.TravellerApiService
import com.client.traveller.data.provider.LocationProvider
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RepositoryImpl(
    private val travellerApiService: TravellerApiService,
    private val userDao: UserDao,
    private val locationProvider: LocationProvider
) : Repository, SafeApiRequest() {

    override suspend fun saveUser(firebaseUser: FirebaseUser) {
        GlobalScope.launch(Dispatchers.IO) {
            val user = User(firebaseUser.displayName, firebaseUser.email, firebaseUser.isEmailVerified)
            userDao.upsert(user)
        }
    }

    override fun getUser(): LiveData<User> {
        return userDao.getUser()
    }

    override suspend fun deleteUser() {
            GlobalScope.launch(Dispatchers.IO){
                userDao.deleteUser()
            }

    }

    override fun initLocationProvider(map: SupportMapFragment, context: Context, savedInstanceState: Bundle?) {
        //TODO przerobić locationProvider na wstrzykiwanie zlerzności (trzeba go wstrzyknać do viewmodel główenj aktywności) i z tego go inicalizować
//        if (!locationProviderInitialized){
            locationProvider.init(map, context, savedInstanceState)
//            locationProviderInitialized = true
//        }
    }

    override fun startLocationUpdates() {
        locationProvider.startLocationUpdates()
    }

    override fun stopLocationUpdates() {
        locationProvider.stopLocationUpdates()
    }

    override fun sendingLocationData(): Boolean {
        return locationProvider.sendingLocationData()
    }

    override fun requestLocationPermission() {
        locationProvider.checkPermissions()
    }


}