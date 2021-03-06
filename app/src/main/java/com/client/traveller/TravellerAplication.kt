package com.client.traveller


import android.app.Application
import android.content.Context
import com.client.traveller.data.db.AppDatabase
import com.client.traveller.data.network.api.directions.DirectionsApiService
import com.client.traveller.data.network.api.geocoding.GeocodingApiService
import com.client.traveller.data.network.api.places.PlacesApiService
import com.client.traveller.data.network.api.wikipedia.WikipediaApiService
import com.client.traveller.data.network.map.MapUtils
import com.client.traveller.data.network.map.MapUtilsImpl
import com.client.traveller.data.provider.PreferenceProvider
import com.client.traveller.data.repository.map.MapRepository
import com.client.traveller.data.repository.map.MapRepositoryImpl
import com.client.traveller.data.repository.message.MessagingRepository
import com.client.traveller.data.repository.message.MessagingRepositoryImpl
import com.client.traveller.data.repository.place.PlacesRepository
import com.client.traveller.data.repository.place.PlacesRepositoryImpl
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.trip.TripRepositoryImpl
import com.client.traveller.data.repository.user.UserRepository
import com.client.traveller.data.repository.user.UserRepositoryImpl
import com.client.traveller.ui.auth.AuthViewModelFactory
import com.client.traveller.ui.chat.ChatViewModelFactory
import com.client.traveller.ui.chat.messeages.MesseageViewModelFactory
import com.client.traveller.ui.home.HomeViewModelFactory
import com.client.traveller.ui.nearby.NearbyPlacesViewModelFactory
import com.client.traveller.ui.settings.SettingsViewModelFactory
import com.client.traveller.ui.splash.SplashViewModelFactory
import com.client.traveller.ui.trip.TripViewModelFactory
import com.client.traveller.ui.tripInfo.TripInfoViewModelFactory
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
        bind() from singleton { DirectionsApiService() }
        bind() from singleton { PlacesApiService() }
        bind() from singleton { GeocodingApiService() }
        bind() from singleton { WikipediaApiService() }
        bind<MapUtils>() with singleton { MapUtilsImpl(instance()) }
        bind() from singleton { AppDatabase(instance()) }
        bind() from singleton { instance<AppDatabase>().userDao() }
        bind<UserRepository>() with singleton {
            UserRepositoryImpl(
                instance(),
                instance()
            )
        }
        bind<MapRepository>() with singleton {
            MapRepositoryImpl(
                instance(),
                instance()
            )
        }
        bind() from singleton { instance<AppDatabase>().tripDao() }
        bind<TripRepository>() with singleton {
            TripRepositoryImpl(
                instance()
            )
        }
        bind() from singleton { instance<AppDatabase>().messeageDao() }
        bind<MessagingRepository>() with singleton {
            MessagingRepositoryImpl(
                instance()
            )
        }
        bind<PlacesRepository>() with singleton {
            PlacesRepositoryImpl(
                instance(),
                instance(),
                instance()
            )
        }
        bind() from provider { AuthViewModelFactory(instance()) }
        bind() from provider {
            HomeViewModelFactory(
                instance(),
                instance(),
                instance(),
                instance(),
                instance()
            )
        }
        bind() from provider { SettingsViewModelFactory() }
        bind() from provider { TripViewModelFactory(instance(), instance(), instance()) }
        bind() from provider { ChatViewModelFactory(instance(), instance(), instance(), instance()) }
        bind() from provider { MesseageViewModelFactory(instance(), instance(), instance()) }
        bind() from provider { SplashViewModelFactory(instance()) }
        bind() from provider {
            NearbyPlacesViewModelFactory(
                instance(),
                instance(),
                instance(),
                instance()
            )
        }
        bind() from provider { TripInfoViewModelFactory(instance(), instance(), instance(), instance())}


    }

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)
    }
}