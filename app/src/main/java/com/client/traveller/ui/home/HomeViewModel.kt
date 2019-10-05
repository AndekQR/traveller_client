package com.client.traveller.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.provider.LocationProvider
import com.client.traveller.data.repository.user.UserRepository
import com.client.traveller.ui.util.Coroutines
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.auth.FirebaseUser

class HomeViewModel(
    private val userRepository: UserRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {

    fun getLoggedInUser(): LiveData<User> {
        return userRepository.getUser()
    }

    fun logoutUser(mGoogleSignInClient: GoogleSignInClient) {
        userRepository.logoutUser(mGoogleSignInClient)
    }

    fun setEmailVerified() {
        Coroutines.io {
            userRepository.setEmailVerifiedAsync()
        }
    }

    fun updateProfile(user: User) {
        userRepository.updateProfile(user)
    }

    fun updateAvatar(user: User, avatarUrl: String) {
        userRepository.updateAvatar(user, avatarUrl)
    }

    fun initLocationProvider(
        map: SupportMapFragment,
        context: Context,
        savedInstanceState: Bundle?
    ) {
        return locationProvider.init(map, context, savedInstanceState)
    }

    fun startLocationUpdates() {
        return locationProvider.startLocationUpdates()
    }

    fun stopLocationUpdates() {
        return locationProvider.stopLocationUpdates()
    }

    fun sendingLocationData(): Boolean {
        return locationProvider.sendingLocationData()
    }

    /**
     * Wysyła email weryfikacyjny do podanego użytkownika
     * metoda aktualizuje pole veriify w encji utkownika
     *
     * @param user użytkownik do którego zostanie wysłany email weryfikacyjny
     */
    fun sendEmailVerification(user: FirebaseUser?) {
        if (user != null)
            userRepository.sendEmailVerification(user) { isSuccessful, exception ->
                if (isSuccessful) {
                    Log.e(javaClass.simpleName, "sendEmailVerification successful")
                } else {
                    Log.e(javaClass.simpleName, exception?.localizedMessage)
                }
            }
    }

}