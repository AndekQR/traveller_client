package com.client.traveller.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class ChatRoomModel(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var participants: ArrayList<String>? = null,
    var messeages: ArrayList<Messeage>? = null
): Serializable

