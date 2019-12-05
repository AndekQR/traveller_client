package com.client.traveller.ui.tripInfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.data.repository.map.MapRepository
import com.client.traveller.data.repository.place.PlacesRepository
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.user.UserRepository

class TripInfoViewModelFactory(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository,
    private val mapRepository: MapRepository,
    private val placesRepository: PlacesRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TripInfoViewModel(tripRepository, userRepository, mapRepository, placesRepository) as T
    }

}