package com.client.traveller.data.repository

import androidx.lifecycle.LiveData
import com.client.traveller.data.db.entities.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Deferred

interface Repository {
    fun saveUser(user: User)
    fun getUser(): LiveData<User>
    fun deleteUserLocal() //jest tylko jeden aktualnie zalogowany!
    suspend fun setEmailVerified()
    fun updateProfile(user: User)
}