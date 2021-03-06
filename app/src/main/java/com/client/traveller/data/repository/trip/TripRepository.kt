package com.client.traveller.data.repository.trip

import android.content.Context
import androidx.lifecycle.LiveData
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User

interface TripRepository {
    suspend fun getAllTrips(): LiveData<List<Trip>>
    suspend fun newTrip(trip: Trip, context: Context): Void?
    fun getCurrentTrip(): LiveData<Trip>
    suspend fun saveTripToLocalDB(trip: Trip, context: Context)
    fun isTripParticipant(trip: Trip, user: User): Boolean
    fun updateTripPersons(trip: Trip, emails: List<String>)
    suspend fun initCurrentTripUpdates()
    suspend fun getTripByUid(tripUid: String): Trip
    fun updateWaypoints(waypoints: List<String>, tripToUpdate: Trip)
}