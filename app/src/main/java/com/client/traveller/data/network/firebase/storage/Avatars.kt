package com.client.traveller.data.network.firebase.storage

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

// TODO zdjęcie z serwisów społcznościowych dodaje się w słabej rozdzielczości
object Avatars {


        internal const val AVATARS = "users_image"
        private const val DEFAULT_AVATAR = "avatar_default.png"


    private fun getAvatarsReference(): StorageReference {
        return FirebaseStorage.getInstance().reference.child(AVATARS)
    }

    fun getDefaultAvatarImageReference(): StorageReference {
        return this.getAvatarsReference().child(DEFAULT_AVATAR)
    }

}