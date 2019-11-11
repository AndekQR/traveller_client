package com.client.traveller.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Messeage(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var uid: String? = null,
    var senderIdFirebase: String? = null,
    var messeage: String? = null,
    var sendDate: Long? = null
) : Serializable