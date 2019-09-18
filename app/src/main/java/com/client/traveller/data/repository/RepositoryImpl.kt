package com.client.traveller.data.repository

import androidx.lifecycle.LiveData
import com.client.traveller.data.db.UserDao
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.db_remote.Users
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.*

class RepositoryImpl(
    private val userDao: UserDao,
    private val usersFirestore: Users
) : Repository {

    override fun saveUser(user: User): Task<Void> {
        GlobalScope.launch(Dispatchers.IO) {
            userDao.upsert(user)
        }
        //operacje na firestore są już asynchroniczne
        return usersFirestore.createUser(user)
    }

    override fun getUser(): LiveData<User> {
        return userDao.getUser()
    }

    override fun deleteUser() {
        GlobalScope.launch(Dispatchers.IO) {
            userDao.deleteUser()

        }

    }

    override suspend fun setEmailVerified() {
        GlobalScope.launch(Dispatchers.IO) {
            userDao.setEmailVerified()
        }
    }
}