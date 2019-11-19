package com.client.traveller.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.api.directions.model.TravelMode
import com.client.traveller.data.repository.map.MapRepository
import com.client.traveller.data.repository.message.MessagingRepository
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.user.UserRepository
import com.client.traveller.ui.util.Coroutines.io
import com.client.traveller.ui.util.format
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository,
    private val mapRepository: MapRepository,
    private val messagingRepository: MessagingRepository,
    private val tripRepository: TripRepository
) : ViewModel() {

    val currentUser: LiveData<User> = this.userRepository.getCurrentUser()
    val currentTrip: LiveData<Trip> = this.tripRepository.getCurrentTrip()

    init {
        viewModelScope.launch { messagingRepository.refreshToken() }
    }

    fun logoutUser(mGoogleSignInClient: GoogleSignInClient) =
        userRepository.logoutUser(mGoogleSignInClient)

    fun setEmailVerified() {
        viewModelScope.launch { userRepository.setEmailVerifiedAsync() }
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
    fun drawRouteToLocation(
        origin: String = mapRepository.getCurrentLocation().format(),
        destination: String,
        locations: List<String>,
        mode: TravelMode = TravelMode.driving
    ) {
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

    suspend fun drawTripRoute(trip: Trip) {
        this.mapRepository.drawTripRoute(trip)
    }
}