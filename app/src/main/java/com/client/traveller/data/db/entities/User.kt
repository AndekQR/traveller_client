package com.client.traveller.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

const val CURRENT_USER_ID = 0

@Entity
data class User(
    //idUserFirebase to id usera które jest zwracane przez firebase Auth
    var idUserFirebase: String? = null,
    var displayName: String? = null,
    var email: String? = null,
    var verified: Boolean = false
) {
    @PrimaryKey(autoGenerate = false)
    var uid: Int = CURRENT_USER_ID
}