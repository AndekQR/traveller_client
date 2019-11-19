package com.client.traveller.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.client.traveller.data.db.entities.Messeage

@Dao
interface MesseageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(messeage: Messeage): Long

    @Query("SELECT * FROM messeage")
    fun getAll(): LiveData<List<Messeage>>

    @Query("DELETE FROM messeage")
    suspend fun deleteAll()
}