package com.client.traveller.ui.tripInfo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.client.traveller.data.db.entities.Trip
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

    val unsavedChanges = MutableLiveData<Boolean>()
    var waypoints = mutableListOf<String>()

    fun logoutUser(mGoogleSignInClient: GoogleSignInClient) =
        userRepository.logoutUser(mGoogleSignInClient)

    suspend fun getNearbyPlaces(location: String): Set<Result> {
        val geocodeResult = this@TripInfoViewModel.mapRepository.geocodeAddress(location).results
        val latlngLocation = if (geocodeResult.isNotEmpty()) geocodeResult.first().geometry.location.toLatLng() else return emptySet()
        val placesResponse = this@TripInfoViewModel.placesRepository.getNearbyPlaces(latlngLocation.formatToApi(), 1000)
        if (placesResponse.isEmpty()) return emptySet()
        val places = mutableListOf<Result>()
        placesResponse.forEach {
            places.addAll(it.results)
        }
        places.shuffle()
        return places.toSet()
    }

    suspend fun geocodeAddress(address: String) = this.mapRepository.geocodeAddress(address)
    fun updateWaypoints(waypoints: List<String>, tripToUpdate: Trip) = this.tripRepository.updateWaypoints(waypoints, tripToUpdate)

}
