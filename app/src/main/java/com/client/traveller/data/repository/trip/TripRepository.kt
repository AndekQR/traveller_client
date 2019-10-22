package com.client.traveller.data.repository.trip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User

interface TripRepository {
    suspend fun getAllTrips(): MutableLiveData<List<Trip>>
    suspend fun newTrip(trip: Trip): Void?
    fun getCurrentTrip(): LiveData<Trip>
    suspend fun setTripAsActual(trip: Trip)
    fun isTripParticipant(trip: Trip, user: User): Boolean
}