package com.client.traveller.data.db.entities

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

const val CURRENT_USER_ID = 0

@Entity
data class User(
    //idUserFirebase to id usera które jest zwracane przez firebase Auth
    var idUserFirebase: String? = null,
    var displayName: String? = null,
    var email: String? = null,
    var verified: Boolean = false,
    var image: String? = null
) {
    //lokalne id
    @PrimaryKey(autoGenerate = false)
    var uid: Int = CURRENT_USER_ID

    override fun toString(): String {
        return "Firebase uid: $idUserFirebase \n" +
                "displayName: $displayName \n" +
                "email: $email \n" +
                "verified: $verified \n " +
                "image: $image"

    }
}