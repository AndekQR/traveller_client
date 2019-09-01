package com.client.traveller.data.repository

import androidx.lifecycle.LiveData
import com.client.traveller.data.db.UserDao
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.SafeApiRequest
import com.client.traveller.data.network.TravellerApiService
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RepositoryImpl(
    private val travellerApiService: TravellerApiService,
    private val userDao: UserDao
) : Repository, SafeApiRequest() {

    override suspend fun saveUser(firebaseUser: FirebaseUser) {
        GlobalScope.launch(Dispatchers.IO) {
            val user = User(firebaseUser.uid, firebaseUser.displayName, firebaseUser.email, firebaseUser.isEmailVerified)
            userDao.upsert(user)
        }
    }

    override fun getUser(): LiveData<User> {
        return userDao.getUser()
    }

    override suspend fun deleteUser() {
            GlobalScope.launch(Dispatchers.IO){
                userDao.deleteUser()
            }

    }

    override suspend fun setEmailVerified() {
        GlobalScope.launch(Dispatchers.IO) {
            userDao.setEmailVerified()
        }
    }
}