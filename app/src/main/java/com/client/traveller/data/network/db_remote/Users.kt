package com.client.traveller.data.network.db_remote

import com.client.traveller.data.db.entities.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference


/**
 * Klasa zarządza użytkownikami w firestore
 */
class Users{

    private val COLLECTION_NAME = "users"

    private fun getUsersCollection(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
    }

    fun  createUser(user: User): Task<Void> {
        return this.getUsersCollection().document(user.idUserFirebase!!).set(user)
    }

    fun getUser(uid: String): Task<DocumentSnapshot> {
        return this.getUsersCollection().document(uid).get()
    }

    fun updateUsername(username: String, uid: String): Task<Void> {
        return this.getUsersCollection().document(uid).update("username", username)
    }

    fun deleteUser(uid: String): Task<Void> {
        return this.getUsersCollection().document(uid).delete()
    }
}