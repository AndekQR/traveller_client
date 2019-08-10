package com.client.traveller

import android.app.Application
import com.client.traveller.data.db.AppDatabase
import com.client.traveller.data.network.NetworkInterceptor
import com.client.traveller.data.network.NetworkInterceptorImpl
import com.client.traveller.data.network.TravellerApiService
import com.client.traveller.data.repository.UserRepository
import com.client.traveller.data.repository.UserRepositoryImpl
import com.client.traveller.ui.auth.AuthViewModelFactory
import com.client.traveller.ui.home.HomeViewModelFactory
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


        bind() from singleton { AppDatabase(instance()) }
        bind() from singleton { instance<AppDatabase>().UserDao() }
        bind<NetworkInterceptor>() with singleton { NetworkInterceptorImpl(instance()) }
        bind() from singleton { TravellerApiService(instance()) }
        bind<UserRepository>() with singleton { UserRepositoryImpl(instance(), instance()) }
        bind() from provider { AuthViewModelFactory(instance()) }
        bind() from singleton { NetworkInterceptorImpl(instance()) }
        bind() from provider { HomeViewModelFactory(instance()) }

    }

    override fun onCreate() {
        super.onCreate()

    }

}