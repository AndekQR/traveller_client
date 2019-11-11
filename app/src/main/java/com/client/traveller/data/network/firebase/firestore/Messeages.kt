package com.client.traveller.data.network.firebase.firestore

import com.client.traveller.data.db.entities.Messeage
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

/**
 * chat zawiera serię wiadomości (messeages)
 */
class Messeages {

    companion object {
        private const val COLLECTION_NAME = "messeages"
    }

    private fun getCollectionReference(chatUid: String): CollectionReference {
        return FirebaseFirestore.getInstance().collection(Chats.COLLECTION_NAME).document(chatUid)
            .collection(COLLECTION_NAME)
    }

    fun saveMesseage(chatUid: String, messeage: Messeage) {
        this.getCollectionReference(chatUid).document(Date().time.toString()).set(messeage)
    }

    fun getMesseages(chatUid: String): CollectionReference {
        return this.getCollectionReference(chatUid)
    }

}