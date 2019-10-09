package com.client.traveller

import android.app.Application
import android.content.Context
import com.client.traveller.data.db.AppDatabase
import com.client.traveller.data.network.firebase.auth.*
import com.client.traveller.data.network.firebase.firestore.Trips

import com.client.traveller.data.network.firebase.firestore.Users
import com.client.traveller.data.network.firebase.storage.Avatars
import com.client.traveller.data.network.map.MapUtils
import com.client.traveller.data.network.map.MapUtilsImpl

import com.client.traveller.data.provider.LocationProvider
import com.client.traveller.data.provider.LocationProviderImpl
import com.client.traveller.data.provider.PreferenceProvider
import com.client.traveller.data.repository.map.MapRepository
import com.client.traveller.data.repository.map.MapRepositoryImpl
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.trip.TripRepositoryImpl
import com.client.traveller.data.repository.user.UserRepository
import com.client.traveller.data.repository.user.UserRepositoryImpl
import com.client.traveller.ui.auth.AuthViewModelFactory
import com.client.traveller.ui.home.HomeViewModelFactory
import com.client.traveller.ui.settings.SettingsViewModelFactory
import com.client.traveller.ui.trips.TripViewModelFactory
import com.google.android.gms.location.LocationServices
import com.jakewharton.threetenabp.AndroidThreeTen


import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class TravellerAplication : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        import(androidXModule(this@TravellerAplication))

        bind() from provider { LocationServices.getFusedLocationProviderClient(instance<Context>()) }
        bind() from singleton { PreferenceProvider(instance()) }
        bind<LocationProvider>() with singleton { LocationProviderImpl(instance(), instance()) }
//        bind<MapUtils>() with singleton { MapUtilsImpl(instance()) }
        bind() from singleton { AppDatabase(instance()) }
        bind() from singleton { Users() }
        bind() from provider { Avatars() }
        bind() from provider { AuthNormal() }
        bind() from provider { AuthGoogle() }
        bind() from provider { AuthFacebook() }
        bind() from provider { AuthUtils() }
        bind() from provider { AuthProvider() }
        bind() from singleton { instance<AppDatabase>().userDao() }
        bind<UserRepository>() with singleton {
            UserRepositoryImpl(
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance()
            )
        }
        bind() from provider { Trips() }
        bind() from singleton { instance<AppDatabase>().tripDao() }
        bind<TripRepository>() with singleton { TripRepositoryImpl(instance(), instance()) }
        bind() from provider { AuthViewModelFactory(instance()) }
        bind() from provider { HomeViewModelFactory(instance(), instance()) }
        bind() from provider { SettingsViewModelFactory(instance()) }
        bind() from provider { TripViewModelFactory(instance(), instance()) }

        bind<MapRepository>() with singleton { MapRepositoryImpl() }

    }

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)
    }

}