package com.client.traveller.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.data.repository.map.MapRepository
import com.client.traveller.data.repository.message.MessagingRepository
import com.client.traveller.data.repository.place.PlacesRepository
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.user.UserRepository

class HomeViewModelFactory(
    private val userRepository: UserRepository,
    private val mapRepository: MapRepository,
    private val messagingRepository: MessagingRepository,
    private val tripRepository: TripRepository,
    private val placesRepository: PlacesRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeViewModel(
            userRepository,
            mapRepository,
            messagingRepository,
            tripRepository,
            placesRepository
        ) as T
    }
}