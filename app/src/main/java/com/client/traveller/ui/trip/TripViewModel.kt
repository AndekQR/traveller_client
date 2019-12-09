package com.client.traveller.ui.trip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.api.directions.model.TravelMode
import com.client.traveller.data.network.api.directions.response.Distance
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

    val currentUser: LiveData<User> = this.userRepository.getCurrentUser()
    val currentTrip: LiveData<Trip> = this.tripRepository.getCurrentTrip()
    lateinit var allTrips: LiveData<List<Trip>>

    var selectedItem: TripListItem? = null
    val searchQuery: MutableLiveData<String> = MutableLiveData()

    fun initAllTripsLiveData() = viewModelScope.launch(Dispatchers.Main) {
        this@TripViewModel.allTrips = this@TripViewModel.tripRepository.getAllTrips()
    }

    suspend fun addTrip(trip: Trip) = tripRepository.newTrip(trip)

    suspend fun tripDistance(
        origin: String,
        destination: String,
        waypoints: ArrayList<String>?,
        mode: TravelMode = TravelMode.driving
    ): Distance? {
        return mapRepository.getDistance(origin, destination, waypoints, mode)
    }

    fun formatDateTime(dateTimeString: String?): String {
        val dateTime = LocalDateTime.parse(dateTimeString)
        val date = "${dateTime.dayOfMonth}.${dateTime.month.value}.${dateTime.year}"
        val time = "${dateTime.hour}:${dateTime.minute}"
        return "$date $time"
    }

    fun isTripParticipant(trip: Trip, user: User) = tripRepository.isTripParticipant(trip, user)
    fun setTripAsActual(trip: Trip) = viewModelScope.launch {
        tripRepository.saveTripToLocalDB(trip)
        tripRepository.initCurrentTripUpdates()
    }

    fun updateTripPersons(tripToUpdate: Trip, emails: List<String>) =
        tripRepository.updateTripPersons(tripToUpdate, emails)

    fun startLocationUpdates() = mapRepository.startLocationUpdates()
    fun stopLocationUpdates() = mapRepository.stopLocationUpdates()
    fun sendingLocationData() = mapRepository.sendingLocationData()
}