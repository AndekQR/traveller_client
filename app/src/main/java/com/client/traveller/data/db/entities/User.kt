package com.client.traveller.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

const val CURRENT_USER_ID = 0

@Entity
data class User(
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var verified: Boolean? = null
){
    @PrimaryKey(autoGenerate = false)
    var uid: Int = CURRENT_USER_ID
}