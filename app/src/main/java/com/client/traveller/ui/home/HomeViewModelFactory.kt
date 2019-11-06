package com.client.traveller.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.data.repository.map.MapRepository
import com.client.traveller.data.repository.message.MessagingRepository
import com.client.traveller.data.repository.user.UserRepository

class HomeViewModelFactory(
    private val userRepository: UserRepository,
    private val mapRepository: MapRepository,
    private val messagingRepository: MessagingRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeViewModel(userRepository, mapRepository, messagingRepository) as T
    }
}