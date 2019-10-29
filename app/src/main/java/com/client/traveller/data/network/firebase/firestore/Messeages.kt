package com.client.traveller.data.network.firebase.firestore

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class Messeages {

    companion object {
        private const val COLLECTION_NAME = "messeages"
    }

    private fun getMesseagesCollection(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
    }
}