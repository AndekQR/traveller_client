package com.client.traveller.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.data.repository.message.CloudMessagingRepository
import com.client.traveller.data.repository.user.UserRepository

class ChatViewModelFactory(
    private val userRepository: UserRepository,
    private val cloudMessagingRepository: CloudMessagingRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ChatViewModel(userRepository, cloudMessagingRepository) as T
    }
}