package com.client.traveller.ui.settings


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.data.provider.LocationProvider

class SettingsViewModelFactory(
    private val locationProvider: LocationProvider
): ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SettingsViewModel(locationProvider) as T
    }
}