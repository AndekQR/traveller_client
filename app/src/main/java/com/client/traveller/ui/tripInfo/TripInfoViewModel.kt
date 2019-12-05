package com.client.traveller.ui.tripInfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
import com.client.traveller.data.repository.map.MapRepository
import com.client.traveller.data.repository.place.PlacesRepository
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.user.UserRepository
import com.client.traveller.ui.util.formatToApi
import com.client.traveller.ui.util.toLatLng
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TripInfoViewModel(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
    private val mapRepository: MapRepository,
    private val placesRepository: PlacesRepository
) : ViewModel() {

    val currentUser = userRepository.getCurrentUser()
    val currentTrip = tripRepository.getCurrentTrip()

    fun logoutUser(mGoogleSignInClient: GoogleSignInClient) =
        userRepository.logoutUser(mGoogleSignInClient)

    suspend fun getNearbyPlaces(location: String): Set<Result> {
        val latlngLocation = this@TripInfoViewModel.mapRepository.geocodeAddress(location).results.first().geometry.location.toLatLng()
        val placesResponse = this@TripInfoViewModel.placesRepository.getNearbyPlaces(latlngLocation.formatToApi(), 1000)
        val places = mutableListOf<Result>()
        placesResponse.forEach {
            places.addAll(it.results)
        }
        places.shuffle()
        return places.toSet()
    }
}
