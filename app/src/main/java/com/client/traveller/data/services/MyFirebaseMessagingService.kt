package com.client.traveller.data.services

import android.util.Log
import com.client.traveller.data.network.firebase.firestore.Tokens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.e(javaClass.simpleName, token)
        this.saveToken(token)
    }

    private fun saveToken(token: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            FirebaseFirestore.getInstance().collection(Tokens.COLLECTION_NAME).document(it.email!!)
                .set(token)
        }
    }
}