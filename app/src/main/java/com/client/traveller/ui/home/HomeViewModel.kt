package com.client.traveller.ui.home

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.api.directions.model.TravelMode
import com.client.traveller.data.repository.map.MapRepository
import com.client.traveller.data.repository.message.MessagingRepository
import com.client.traveller.data.repository.place.PlacesRepository
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.user.UserRepository
import com.client.traveller.ui.util.format
import com.client.traveller.ui.util.formatToApi
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository,
    private val mapRepository: MapRepository,
    private val messagingRepository: MessagingRepository,
    private val tripRepository: TripRepository,
    private val placesRepository: PlacesRepository
) : ViewModel() {

    val currentUser: LiveData<User> = this.userRepository.getCurrentUser()
    val currentTrip: LiveData<Trip> = this.tripRepository.getCurrentTrip()
    var currentLocation: Location? = null

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

    suspend fun drawRouteToLocation(
        origin: String ,
        destination: String,
        locations: List<String>?,
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

    suspend fun centerRoad(startAddress: String, waypoints: ArrayList<String>?, endAddress: String) = this.mapRepository.centerCameraOnRoute(startAddress, waypoints, endAddress)
    fun elementsOnMap() = this.mapRepository.elementOnMap()
    suspend fun drawMarkerNearbyPlaces() {
        val marker = this.mapRepository.getActualMarker()
        marker?.let {
            val nearbyPlaces = this.placesRepository.getNearbyPlaces(it.position.formatToApi())
            this.mapRepository.drawNearbyPlaceMarkers(nearbyPlaces)
        }
    }
    suspend fun drawRouteToMainMarker(location: Location) = this.mapRepository.drawRouteToMainMarker(location)
    fun centerCameraOnLocation(location: LatLng) = this.mapRepository.centerCameraOnLocation(location)
    fun drawTripParticipants(trip: Trip, currentUser: User?) {
        this.mapRepository.drawTripUsersLocation(trip.uid!!, currentUser)
    }
}