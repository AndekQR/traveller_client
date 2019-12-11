package com.client.traveller.ui.nearby

import android.content.Context
import android.location.Location
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
import com.client.traveller.data.repository.map.MapRepository
import com.client.traveller.data.repository.place.PlacesRepository
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.user.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class NearbyPlacesViewModel(
    private val placesRepository: PlacesRepository,
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
    private val mapRepository: MapRepository
) : ViewModel() {

    val currentUser = userRepository.getCurrentUser()
    val currentTrip = tripRepository.getCurrentTrip()
    var currentLocation: Location? = null

    // fraza w wyszukiwarce
    val searchQuery = MutableLiveData<String>()
    // zaznaczone typy w oknu dialogowym do filtrowania
    var checkedItems = this.getPlacesSearchedTypes().map { false }.toMutableList()

    // miejsca zwrócone przez places api
    private var _searchedPlaces: MutableLiveData<Set<Result>> = MutableLiveData(emptySet())
    val searchedPlaces: LiveData<Set<Result>>
        get() = _searchedPlaces

    private var originalListOfPlaces: Set<Result> = setOf()

    // pobieranie miejsc w poblizu z places api
    suspend fun findNearbyPlaces(latlng: String) = placesRepository.getNearbyPlaces(latlng)

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

    /**
     * Zwraca listę typów miejsc, jakie były brane pod uwagę przy wyszukiwaniu miejsc
     */
    fun getPlacesSearchedTypes() = placesRepository.getSearchedTypes()

    fun initOriginalListOfPlaces(places: Set<Result>) {
        if (this.originalListOfPlaces.isEmpty())
            this.originalListOfPlaces = places
    }

    fun getOriginalListOfPlaces(): Set<Result> {
        return this.originalListOfPlaces
    }

    suspend fun getPlaceDetails(placeId: String) = placesRepository.getPlaceDetail(placeId)

    fun initMap(
        map: SupportMapFragment,
        context: Context,
        savedInstanceState: Bundle?
    ) = mapRepository.initializeMap(map, context, savedInstanceState)

    suspend fun getWikipediaPrefixes(query: String) = this.placesRepository.getPrefixes(query)
    suspend fun getWikipediaPageSummary(pageTitle: String) =
        this.placesRepository.getPageSummary(pageTitle)

    suspend fun getWikipediaPageSectionsText(pageTitle: String) =
        this.placesRepository.getPageSections(pageTitle)

    fun disableMapDragging() = this.mapRepository.disableMapDragging()
    fun centerOnLocation(location: LatLng, putMarker: Boolean = false) = this.mapRepository.centerCameraOnLocation(location, putMarker)
}