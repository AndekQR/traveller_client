package com.client.traveller.data.network.firebase.firestore

import android.util.Log
import com.client.traveller.data.db.entities.User
import com.client.traveller.ui.util.toLocalUser
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.suspendCoroutine

class Tokens {
    companion object {
        const val COLLECTION_NAME = "cloud_messaging_tokens"
    }

    private var token: String? = null

    private fun getTokensCollection(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
    }

    fun saveToken(token: String, user: User? = FirebaseAuth.getInstance().currentUser?.toLocalUser()) {
        user?.let {
            this.token = token
            this.getTokensCollection().document(it.email!!).set(mapOf("token" to token))
        }
    }

    suspend fun getCurrentToken() = suspendCoroutine<String?> { continuation ->
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resumeWith(Result.success(task.result?.token))
                } else {
                    continuation.resumeWith(Result.failure(task.exception!!))
                }
            }
    }
}