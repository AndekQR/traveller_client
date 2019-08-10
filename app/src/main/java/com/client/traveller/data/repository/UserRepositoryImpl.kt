package com.client.traveller.data.repository

import androidx.lifecycle.LiveData
import com.client.traveller.data.db.UserDao
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.SafeApiRequest
import com.client.traveller.data.network.TravellerApiService
import com.client.traveller.data.network.response.LoginResponse
import com.client.traveller.ui.util.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    private val travellerApiService: TravellerApiService,
    private val userDao: UserDao
) : UserRepository, SafeApiRequest() {


    override suspend fun login(email: String, password: String): LoginResponse {
        val pass = mapOf<String, String>("username" to email, "password" to password)
        return apiRequest{travellerApiService.userLogin(pass)}
    }

    override suspend fun register(firstName: String, lastName: String, email: String, password: String): LoginResponse {
        val data = mapOf("firstName" to firstName, "lastName" to lastName, "email" to email, "password" to password)
        return apiRequest{travellerApiService.register(data)}
    }

    override suspend fun saveUser(user: User) {
        GlobalScope.launch(Dispatchers.IO) {
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

}