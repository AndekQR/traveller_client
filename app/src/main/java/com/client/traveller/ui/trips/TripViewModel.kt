package com.client.traveller.ui.trips

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.user.UserRepository

class TripViewModel(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    suspend fun getAllTrips(): LiveData<List<Trip>> {
        return tripRepository.getAllTrips()
    }

    fun getLoggedInUser(): LiveData<User> {
        return userRepository.getUser()
    }

    suspend fun addTripAsync(trip: Trip): Void? {
        return tripRepository.newTrip(trip)
    }

}