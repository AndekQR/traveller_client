package com.client.traveller.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.client.traveller.data.db.entities.Trip

@Dao
interface TripDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(trip: Trip): Long

    @Query("SELECT * FROM trip")
    fun getCurrentTrip(): LiveData<Trip>

    @Query("DELETE FROM trip")
    fun deleteCurrentTrip()
}