package com.client.traveller.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.client.traveller.data.db.entities.CURRENT_TRIP_ID
import com.client.traveller.data.db.entities.Trip

@Dao
interface TripDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(trip: Trip): Long

    @Query("SELECT * FROM trip WHERE trip.id=$CURRENT_TRIP_ID")
    fun getCurrentTrip(): LiveData<Trip>

    @Query("DELETE FROM trip WHERE id=$CURRENT_TRIP_ID")
    fun deleteCurrentTrip()

    @Query("SELECT persons FROM trip WHERE id=$CURRENT_TRIP_ID")
    fun getCurrentTripPersonsEmail(): LiveData<List<String>>

    @Query("SELECT * FROM trip WHERE trip.id=$CURRENT_TRIP_ID")
    fun getCurrentTripNonLive(): Trip
}