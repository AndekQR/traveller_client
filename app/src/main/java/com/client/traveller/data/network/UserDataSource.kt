//package com.client.traveller.data.network
//
//import androidx.lifecycle.LiveData
//import com.client.traveller.data.db.entities.User
//import com.client.traveller.data.network.response.LoginResponse
//import retrofit2.Response
//
//interface UserDataSource {
//    val downloadedUserData: LiveData<LoginResponse>
//
//    suspend fun fetchUserData(
//        username: String,
//        password: String
//    )
//}