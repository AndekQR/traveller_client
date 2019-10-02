package com.client.traveller.data.repository.trip

import androidx.lifecycle.MutableLiveData
import com.client.traveller.data.db.entities.Trip

interface TripRepository {
    suspend fun getAllTrips(): MutableLiveData<List<Trip>>
    suspend fun newTrip(trip: Trip): Void?
}