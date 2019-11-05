package com.client.traveller.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.client.traveller.data.db.entities.ChatRoomModel

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(chat: ChatRoomModel): Long

}