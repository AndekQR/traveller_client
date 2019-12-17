package com.client.traveller.ui.splash

import androidx.lifecycle.ViewModel
import com.client.traveller.data.repository.user.UserRepository

class SplashViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    suspend fun getCurrentUser() = this.userRepository.getCurrentUserNonLive()
}