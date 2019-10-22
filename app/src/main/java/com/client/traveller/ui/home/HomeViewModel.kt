package com.client.traveller.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.map.directions.model.TravelMode
import com.client.traveller.data.provider.LocationProvider
import com.client.traveller.data.repository.map.MapRepository
import com.client.traveller.data.repository.user.UserRepository
import com.client.traveller.ui.util.Coroutines
import com.client.traveller.ui.util.Coroutines.io
import com.client.traveller.ui.util.format
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.auth.FirebaseUser

class HomeViewModel(
    private val userRepository: UserRepository,
    private val mapRepository: MapRepository
) : ViewModel() {

    fun getLoggedInUser() = userRepository.getUser()
    fun logoutUser(mGoogleSignInClient: GoogleSignInClient) = userRepository.logoutUser(mGoogleSignInClient)

    fun setEmailVerified() {
        io {
            userRepository.setEmailVerifiedAsync()
        }
    }

    fun updateProfile(user: User) = userRepository.updateProfile(user)
    fun updateAvatar(user: User, avatarUrl: String) = userRepository.updateAvatar(user, avatarUrl)

    fun initMap(
        map: SupportMapFragment,
        context: Context,
        savedInstanceState: Bundle?
    ) = mapRepository.initializeMap(map, context, savedInstanceState)

    fun startLocationUpdates() = mapRepository.startLocationUpdates()
    fun stopLocationUpdates() = mapRepository.stopLocationUpdates()
    fun sendingLocationData() = mapRepository.sendingLocationData()
    fun centerOnMe() = mapRepository.centerCurrentLocation()
    fun drawRouteToLocation(origin: String = mapRepository.getCurrentLocation().format(), destination: String, locations: List<String>, mode: TravelMode = TravelMode.driving){
        mapRepository.drawRouteToLocation(origin, destination, locations, mode)
    }
    fun clearMap() = mapRepository.clearMap()


    /**
     * Wysyła email weryfikacyjny do podanego użytkownika
     * metoda aktualizuje pole veriify w encji utkownika
     *
     * @param user użytkownik do którego zostanie wysłany email weryfikacyjny
     */
    fun sendEmailVerification(user: FirebaseUser?) {
        if (user != null)
            userRepository.sendEmailVerification(user) { isSuccessful, exception ->
                if (isSuccessful) {
                    Log.e(javaClass.simpleName, "sendEmailVerification successful")
                } else {
                    Log.e(javaClass.simpleName, exception?.localizedMessage)
                }
            }
    }
}