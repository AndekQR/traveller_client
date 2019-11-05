package com.client.traveller.data.network.firebase.firestore.model

import androidx.room.PrimaryKey
import java.io.Serializable

data class ChatFirestoreModel(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var participantsUid: Map<String, Boolean>? = null,
    var participantsNumber: Int? = null,
    var uid: String? = null
): Serializable {
    init {
        this.uid = this.randomUid()
    }

    private fun randomUid(): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz"
        return (1..35)
            .map { allowedChars.random() }
            .joinToString("")
    }
}