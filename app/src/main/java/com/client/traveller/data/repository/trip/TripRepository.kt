package com.client.traveller.data.repository.trip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import kotlinx.coroutines.Job

interface TripRepository {
    suspend fun getAllTrips(): LiveData<List<Trip>>
    suspend fun newTrip(trip: Trip): Void?
    fun getCurrentTrip(): LiveData<Trip>
    suspend fun saveTripToLocalDB(trip: Trip)
    fun isTripParticipant(trip: Trip, user: User): Boolean
    fun updateTripPersons(trip: Trip, emails: List<String>)
    suspend fun initCurrentTripUpdates()
    suspend fun getTripByUid(tripUid: String): Trip
}