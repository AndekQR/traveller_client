package com.client.traveller.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

const val CURRENT_TRIP_ID = 0

/**
 * Dane wycieczki tylko w której aktualnie użytkownik uczestniczy
 */
@Entity
data class Trip(
    var name: String? = null,
    var persons: ArrayList<String>? = null, // emaile uczestników
    var startDate: String? = null, //godzina data
    var endDate: String? = null,
    var startAddress: String? = null,
    var endAddress: String? = null,
    var waypoints: ArrayList<String>? = null,
    var author: User? = null,
    var uid: String? = null
) : Serializable {

    @PrimaryKey(autoGenerate = false)
    var id: Int = CURRENT_TRIP_ID


}
