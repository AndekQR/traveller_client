package com.client.traveller.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.repository.UserRepository
import com.client.traveller.ui.util.Coroutines

class HomeViewModel(
    private val userRepository: UserRepository
): ViewModel() {

    fun getLoggedInUser(): LiveData<User> {
        return userRepository.getUser()
    }

    fun logoutUser(){
        Coroutines.io {
            userRepository.deleteUser()
        }
    }

}