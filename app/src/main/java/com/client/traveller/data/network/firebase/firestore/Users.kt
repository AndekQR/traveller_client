package com.client.traveller.data.network.firebase.firestore

import com.client.traveller.data.db.entities.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


/**
 * Klasa zarządza użytkownikami w firestore
 */
object Users {


    const val COLLECTION_NAME = "users"


    private fun getUsersCollection(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
    }


    fun createUser(user: User): Task<Void> {
        return getUsersCollection().document(user.idUserFirebase!!).set(user)
    }

    fun getUserByUid(uid: String): Task<DocumentSnapshot> {
        return getUsersCollection().document(uid).get()
    }

    fun getUserByEmail(email: String): Query {
        return getUsersCollection().whereEqualTo("email", email)
    }

    fun updateUsername(uid: String, username: String): Task<Void> {
        return getUsersCollection().document(uid).update("displayName", username)
    }

    fun deleteUser(uid: String): Task<Void> {
        return getUsersCollection().document(uid).delete()
    }

    fun updateImage(uid: String, uri: String): Task<Void> {
        return getUsersCollection().document(uid).update("image", uri)
    }

    fun updateEmail(uid: String, email: String): Task<Void> {
        return getUsersCollection().document(uid).update("email", email)
    }

    fun changeVerifiedStatus(uid: String? = null, state: Boolean): Task<Void>? {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null && currentUserUid != null) {
            return getUsersCollection().document(currentUserUid).update("verified", state)
        } else if (uid != null) {
            return getUsersCollection().document(uid).update("verified", state)
        }
        return null
    }

}