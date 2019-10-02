package com.client.traveller.ui.trips

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.user.UserRepository
import com.client.traveller.ui.util.lazyDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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