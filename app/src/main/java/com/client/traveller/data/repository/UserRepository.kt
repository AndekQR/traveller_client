package com.client.traveller.data.repository

import androidx.lifecycle.LiveData
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.response.LoginResponse

interface UserRepository {

    suspend fun login(email: String, password: String): LoginResponse
    suspend fun register(firstName:String, lastName: String, email: String, password: String):LoginResponse
    suspend fun saveUser(user: User)
    fun getUser(): LiveData<User>
    suspend fun deleteUser() //jest tylko jeden aktualnie zalogowany!
}