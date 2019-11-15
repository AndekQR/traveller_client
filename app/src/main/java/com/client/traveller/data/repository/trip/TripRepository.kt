package com.client.traveller.data.repository.trip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import kotlinx.coroutines.Job

interface TripRepository {
    suspend fun getAllTrips(): MutableLiveData<List<Trip>>
    suspend fun newTrip(trip: Trip): Void?
    fun getCurrentTrip(): LiveData<Trip>
    fun saveTripToLocalDB(trip: Trip): Job
    fun isTripParticipant(trip: Trip, user: User): Boolean
    fun updateTripPersons(trip: Trip, emails: List<String>)
    fun initCurrentTripUpdates(): Job
    suspend fun getTripByUid(tripUid: String): Trip
}