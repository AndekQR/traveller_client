package com.client.traveller

import android.app.Application
import android.content.Context
import com.client.traveller.data.db.AppDatabase
import com.client.traveller.data.network.NetworkInterceptor
import com.client.traveller.data.network.NetworkInterceptorImpl
import com.client.traveller.data.network.TravellerApiService
import com.client.traveller.data.provider.LocationProvider
import com.client.traveller.data.provider.LocationProviderImpl
import com.client.traveller.data.provider.PreferenceProvider
import com.client.traveller.data.repository.Repository
import com.client.traveller.data.repository.RepositoryImpl
import com.client.traveller.ui.auth.AuthViewModelFactory
import com.client.traveller.ui.home.HomeViewModelFactory
import com.client.traveller.ui.settings.SettingsViewModelFactory
import com.google.android.gms.location.LocationServices


import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class TravellerAplication: Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        import(androidXModule(this@TravellerAplication))

        bind() from provider { LocationServices.getFusedLocationProviderClient(instance<Context>()) }
        bind() from singleton { PreferenceProvider(instance()) }
        bind<LocationProvider>() with singleton { LocationProviderImpl(instance(), instance()) }
        bind() from singleton { AppDatabase(instance()) }
        bind() from singleton { instance<AppDatabase>().UserDao() }
        bind<NetworkInterceptor>() with singleton { NetworkInterceptorImpl(instance()) }
        bind() from singleton { TravellerApiService(instance()) }
        bind<Repository>() with singleton { RepositoryImpl(instance(), instance(), instance()) }
        bind() from provider { AuthViewModelFactory(instance()) }
        bind() from singleton { NetworkInterceptorImpl(instance()) }
        bind() from provider { HomeViewModelFactory(instance()) }
        bind() from provider { SettingsViewModelFactory(instance()) }




    }

    override fun onCreate() {
        super.onCreate()


    }

}