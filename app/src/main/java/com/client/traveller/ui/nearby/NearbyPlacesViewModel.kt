package com.client.traveller.ui.nearby

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Photo
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
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

    private var _searchedPlaces: MutableLiveData<Set<Result>> = MutableLiveData()
    val searchedPlaces: LiveData<Set<Result>>
        get() = _searchedPlaces

    suspend fun findNearbyPlaces() = placesRepository.getNearbyPlaces()

    fun logoutUser(mGoogleSignInClient: GoogleSignInClient) =
        userRepository.logoutUser(mGoogleSignInClient)

    fun updateSearchedPlaces(places: Set<Result>)  {
        _searchedPlaces.value = places
    }

    fun getPhoto(photoReference: String?, width: Int?): String? {
        if (photoReference == null || width == null)
            return null
        return placesRepository.getPhotoUrl(photoReference, width)
    }
}