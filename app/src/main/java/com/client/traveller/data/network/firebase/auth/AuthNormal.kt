package com.client.traveller.data.network.firebase.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class AuthNormal {

    fun login(username: String, password: String): Task<AuthResult> {
        return FirebaseAuth.getInstance().signInWithEmailAndPassword(username, password)
    }

    fun resetEmail(email: String): Task<Void> {
        return FirebaseAuth.getInstance().sendPasswordResetEmail(email)
    }

    fun createUser(email: String, password: String): Task<AuthResult> {
        return FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
    }

    fun logout(){
        FirebaseAuth.getInstance().signOut()
    }
}