package com.client.traveller.ui.trip

import androidx.lifecycle.*
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.map.directions.model.TravelMode
import com.client.traveller.data.network.map.directions.response.Distance
import com.client.traveller.data.repository.map.MapRepository
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime

class TripViewModel(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
    private val mapRepository: MapRepository
) : ViewModel() {

    private var _currentUser: MutableLiveData<User> = MutableLiveData()
    val currentUser: LiveData<User>
        get() = _currentUser
    private lateinit var currentUserObserver: Observer<User>

    private var _currentTrip: MutableLiveData<Trip> = MutableLiveData()
    val currentTrip: LiveData<Trip>
        get() = _currentTrip
    private lateinit var currentTripObserver: Observer<Trip>

    private var _allTrips: MutableLiveData<List<Trip>> = MutableLiveData()
    val allTrips: LiveData<List<Trip>>
        get() = _allTrips
    private lateinit var allTripsObserver: Observer<List<Trip>>

    var selectedItem: TripListItem? = null
    val searchQuery: MutableLiveData<String> = MutableLiveData()

    init {
        this.initLiveData()
    }

    private fun initLiveData() = viewModelScope.launch(Dispatchers.Main) {
        currentUserObserver = Observer { user ->
            if (user == null) return@Observer
            _currentUser.value = user
        }
        userRepository.getUser().observeForever(currentUserObserver)

        currentTripObserver = Observer { trip ->
            if (trip == null) return@Observer
            _currentTrip.value = trip
        }
        tripRepository.getCurrentTrip().observeForever(currentTripObserver)

        allTripsObserver = Observer { trips ->
            if (trips == null) return@Observer
            _allTrips.value = trips
        }
        tripRepository.getAllTrips().observeForever(allTripsObserver)
    }

    suspend fun addTrip(trip: Trip): Void? {
        return tripRepository.newTrip(trip)
    }

    // TODO trzeba dodać waypointy do wyznaczania długości
    suspend fun tripDistance(
        origin: String,
        destination: String,
        mode: TravelMode = TravelMode.driving
    ): Distance? {
        return mapRepository.getDistance(origin, destination, mode)
    }

    fun formatDateTime(dateTimeString: String?): String {
        val dateTime = LocalDateTime.parse(dateTimeString)
        val date = "${dateTime.dayOfMonth}.${dateTime.month.value}.${dateTime.year}"
        val time = "${dateTime.hour}:${dateTime.minute}"
        return "$date $time"
    }

    fun isTripParticipant(trip: Trip, user: User) = tripRepository.isTripParticipant(trip, user)
    suspend fun setTripAsActual(trip: Trip) = tripRepository.saveTripToLocalDB(trip)
    fun updateTripPersons(tripToUpdate: Trip, emails: List<String>) =
        tripRepository.updateTripPersons(tripToUpdate, emails)

    private fun removeObservers() = viewModelScope.launch(Dispatchers.IO) {
        userRepository.getUser().removeObserver(currentUserObserver)
        tripRepository.getCurrentTrip().removeObserver(currentTripObserver)
        tripRepository.getAllTrips().removeObserver(allTripsObserver)
    }

    override fun onCleared() {
        super.onCleared()

        this.removeObservers()
    }
}