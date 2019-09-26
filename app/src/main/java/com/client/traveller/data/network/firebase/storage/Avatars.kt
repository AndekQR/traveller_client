package com.client.traveller.data.network.firebase.storage

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.InputStream

//TODO trzeba sprawdzic dodawanie zdjec z serwisow spolecznosciowych do storage
// z google dodaja się zle, a z facebook w ogóle
class Avatars {

    companion object {
        internal const val AVATARS = "users_image"
        private const val DEFAULT_AVATAR = "avatar_default.png"
    }

    private fun getAvatarsReference(): StorageReference {
        return FirebaseStorage.getInstance().reference.child(AVATARS)
    }

    fun getDefaultAvatarImageReference(): StorageReference {
        return this.getAvatarsReference().child(DEFAULT_AVATAR)
    }

}