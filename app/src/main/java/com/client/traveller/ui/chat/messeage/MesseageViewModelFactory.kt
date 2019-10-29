package com.client.traveller.ui.chat.messeage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.data.repository.message.CloudMessagingRepository
import com.client.traveller.data.repository.user.UserRepository

class MesseageViewModelFactory(
    private val userRepository: UserRepository,
    private val cloudMessagingRepository: CloudMessagingRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MesseageViewModel(userRepository, cloudMessagingRepository) as T
    }
}