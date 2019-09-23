package com.client.traveller.data.network.firebase.firestore

import android.net.Uri
import android.util.Log
import com.client.traveller.data.db.entities.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference


/**
 * Klasa zarządza użytkownikami w firestore
 */
class Users{

    companion object{
        private const val COLLECTION_NAME = "users"
    }

    private fun getUsersCollection(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
    }

    fun  createUser(user: User): Task<Void> {
        return this.getUsersCollection().document(user.idUserFirebase!!).set(user)
    }

    fun getUser(uid: String): Task<DocumentSnapshot> {
        return this.getUsersCollection().document(uid).get()
    }

    fun updateUsername(uid: String, username: String): Task<Void> {
        return this.getUsersCollection().document(uid).update("displayName", username)
    }

    fun deleteUser(uid: String): Task<Void> {
        return this.getUsersCollection().document(uid).delete()
    }

    fun getCurrentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    fun updateImage(uid: String, uri: String): Task<Void>{
        return this.getUsersCollection().document(uid).update("image", uri)
    }

    fun updateEmail(uid: String, email: String): Task<Void> {
        return this.getUsersCollection().document(uid).update("email", email)
    }

}