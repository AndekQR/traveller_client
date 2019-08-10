//package com.client.traveller.data.network
//
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import com.client.traveller.data.db.entities.User
//import com.client.traveller.data.network.response.LoginResponse
//import com.client.traveller.ui.util.ApiException
//import retrofit2.Response
//import java.lang.Exception
//
//class UserDataSourceImpl(
//    private val travellerApiService: TravellerApiService
//): UserDataSource, SafeApiRequest() {
//
//    private val _downloadedUserData = MutableLiveData<LoginResponse>()
//    override val downloadedUserData: LiveData<LoginResponse>
//        get() = _downloadedUserData
//
//
//    override suspend fun fetchUserData(username: String, password: String) {
//        try {
//            val fetchedUserData = apiRequest {
//                travellerApiService.userLogin(username, password)
//            }
//
//            _downloadedUserData.postValue(fetchedUserData)
//
//        }
//        catch (e: ApiException){
//            throw ApiException(e.message.toString())
//        }
//    }
//}