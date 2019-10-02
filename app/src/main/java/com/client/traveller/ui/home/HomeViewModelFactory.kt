package com.client.traveller.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.data.provider.LocationProvider
import com.client.traveller.data.repository.user.UserRepository

class HomeViewModelFactory(
    private val userRepository: UserRepository,
    private val locationProvider: LocationProvider
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeViewModel(userRepository, locationProvider) as T
    }
}