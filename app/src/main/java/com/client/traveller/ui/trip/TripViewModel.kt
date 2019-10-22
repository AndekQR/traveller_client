package com.client.traveller.ui.trip

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.map.directions.model.TravelMode
import com.client.traveller.data.network.map.directions.response.Distance
import com.client.traveller.data.repository.map.MapRepository
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.user.UserRepository
import org.threeten.bp.LocalDateTime

class TripViewModel(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
    private val mapRepository: MapRepository
) : ViewModel() {

    suspend fun getAllTrips(): LiveData<List<Trip>> {
        return tripRepository.getAllTrips()
    }

    fun getLoggedInUser(): LiveData<User> {
        return userRepository.getUser()
    }
    fun getCurrentTrip() = tripRepository.getCurrentTrip()

    suspend fun addTrip(trip: Trip): Void? {
        return tripRepository.newTrip(trip)
    }

    // TODO trzeba dodać waypointy do wyznaczania długości
    suspend fun tripDistance(origin: String, destination: String, mode: TravelMode = TravelMode.driving): Distance? {
        return mapRepository.getDistance(origin, destination, mode)
    }

    fun formatDateTime(dateTimeString: String?): String {
        val dateTime = LocalDateTime.parse(dateTimeString)
        val date = "${dateTime.dayOfMonth}.${dateTime.month.value}.${dateTime.year}"
        val time = "${dateTime.hour}:${dateTime.minute}"
        return "$date $time"
    }

    fun isTripParticipant(trip: Trip, user: User) = tripRepository.isTripParticipant(trip, user)


}