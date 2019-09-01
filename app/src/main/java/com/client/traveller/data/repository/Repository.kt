package com.client.traveller.data.repository

import androidx.lifecycle.LiveData
import com.client.traveller.data.db.entities.User
import com.google.firebase.auth.FirebaseUser

interface Repository {
    suspend fun saveUser(firebaseUser: FirebaseUser)
    fun getUser(): LiveData<User>
    suspend fun deleteUser() //jest tylko jeden aktualnie zalogowany!
    suspend fun setEmailVerified()
}