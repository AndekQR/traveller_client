package com.client.traveller.ui.util

import com.client.traveller.data.db.entities.User
import com.google.firebase.auth.FirebaseUser

fun FirebaseUser.toLocalUser(displayName: String? = null): User {

    return User(
        this.uid,
        displayName ?: this.displayName,
        this.email,
        this.isEmailVerified,
        if (this.photoUrl == null) null else this.photoUrl.toString()
    )
}