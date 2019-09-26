package com.client.traveller.data.network.firebase.firestore

import android.util.Log
import com.client.traveller.data.db.entities.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


/**
 * Klasa zarządza użytkownikami w firestore
 */
class Users {

    companion object {
        private const val COLLECTION_NAME = "users"
    }

    private fun getUsersCollection(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
    }

    fun createUser(user: User): Task<Void> {
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

    fun updateImage(uid: String, uri: String): Task<Void> {
        return this.getUsersCollection().document(uid).update("image", uri)
    }

    fun updateEmail(uid: String, email: String): Task<Void> {
        return this.getUsersCollection().document(uid).update("email", email)
    }

    fun changeVerifiedStatus(uid: String? = null, state: Boolean): Task<Void>? {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null && currentUserUid != null) {
            return this.getUsersCollection().document(currentUserUid).update("verified", state)
        } else if (uid != null) {
            return this.getUsersCollection().document(uid).update("verified", state)
        }
        return null
    }

    //TODO mozna cos z tym zrobic zamiast monualnie sprawdzac
    fun checkUserExists(uid: String): Boolean {
        var user: DocumentSnapshot? = null
        val task = getUsersCollection().document(uid).get()
            .addOnCompleteListener {
                user = it.result
            }
        user?.let {
            Log.e(javaClass.simpleName, "weszlo")
            return it.exists()
        }
        return false
    }
}