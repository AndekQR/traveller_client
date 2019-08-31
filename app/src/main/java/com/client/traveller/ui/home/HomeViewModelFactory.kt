package com.client.traveller.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.data.provider.LocationProvider
import com.client.traveller.data.repository.Repository

class HomeViewModelFactory(
    private val repository: Repository,
    private val locationProvider: LocationProvider
): ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeViewModel(repository, locationProvider) as T
    }
}