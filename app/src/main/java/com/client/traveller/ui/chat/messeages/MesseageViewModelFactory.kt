package com.client.traveller.ui.chat.messeages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.data.repository.message.MessagingRepository
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.user.UserRepository

class MesseageViewModelFactory(
    private val userRepository: UserRepository,
    private val messagingRepository: MessagingRepository,
    private val tripRepository: TripRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MesseageViewModel(userRepository, messagingRepository, tripRepository) as T
    }
}