package com.client.traveller.ui.nearby

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
import com.client.traveller.data.repository.place.PlacesRepository
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.user.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class NearbyPlacesViewModel(
    private val placesRepository: PlacesRepository,
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val currentUser = userRepository.getUser()
    val currentTrip = tripRepository.getCurrentTrip()

    // fraza w wyszukiwarce
    val searchQuery = MutableLiveData<String>()
    // zaznaczone typy w oknu dialogowym do filtrowania
    var checkedItems = this.getPlacesSearchedTypes().map { false }.toMutableList()

    // miejsca zwrócone przez places api
    private var _searchedPlaces: MutableLiveData<Set<Result>> = MutableLiveData()
    val searchedPlaces: LiveData<Set<Result>>
        get() = _searchedPlaces

    private var originalListOfPlaces: Set<Result> = setOf()

    // pobieranie miejsc w poblizu z places api
    suspend fun findNearbyPlaces() = placesRepository.getNearbyPlaces()

    fun logoutUser(mGoogleSignInClient: GoogleSignInClient) =
        userRepository.logoutUser(mGoogleSignInClient)

    /**
     * aktualizacja live data z miejscami w pobliżu
     */
    fun updateSearchedPlaces(places: Set<Result>) {
        _searchedPlaces.value = places
    }

    fun getPhoto(photoReference: String?, width: Int?): String? {
        if (photoReference == null || width == null)
            return null
        return placesRepository.getPhotoUrl(photoReference, width)
    }

    fun getPlacesSearchedTypes() = placesRepository.getSearchedTypes()

    fun initOriginalListOfPlaces(places: Set<Result>) {
        if (this.originalListOfPlaces.isEmpty())
            this.originalListOfPlaces = places
    }

    fun getOriginalListOfPlaces(): Set<Result> {
        return this.originalListOfPlaces
    }
}