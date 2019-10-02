package com.client.traveller.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

/**
 * Dane wycieczki tylko w której aktualnie użytkownik uczestniczy
 */
@Entity
data class Trip(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    var name: String? = null,
    var persons: List<String>? = null, // lista uid firebase userow??
    var start: String? = null, //godzzina data
    var end: String? = null,
    var startAddress: String? = null,
    var endAddress: String? = null,
    var waypoints: List<String>? = null,
    var author: User? = null

)
