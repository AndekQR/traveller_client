package com.client.traveller.ui.nearby

import androidx.lifecycle.ViewModel
import com.client.traveller.data.repository.place.PlacesRepository
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.user.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class NearbyPlacesViewModel(
    private val placesRepository: PlacesRepository,
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository
): ViewModel() {

    val currentUser = userRepository.getUser()
    val currentTrip = tripRepository.getCurrentTrip()

    suspend fun findNearbyPlaces() = placesRepository.getNearbyPlaces()
    fun logoutUser(mGoogleSignInClient: GoogleSignInClient) =
        userRepository.logoutUser(mGoogleSignInClient)
}