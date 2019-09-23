package com.client.traveller.ui.home

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.provider.LocationProvider
import com.client.traveller.data.repository.Repository
import com.client.traveller.ui.util.Coroutines
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

class HomeViewModel(
    private val repository: Repository,
    private val locationProvider: LocationProvider
) : ViewModel() {

    fun getLoggedInUser(): LiveData<User> {
        return repository.getUser()
    }

    fun logoutUser(mGoogleSignInClient: GoogleSignInClient) {
        Coroutines.io {
            repository.deleteUserLocal()
        }
        FirebaseAuth.getInstance().signOut()
        mGoogleSignInClient.signOut()
        LoginManager.getInstance().logOut()
    }

    fun setEmailVerified() {
        Coroutines.io {
            repository.setEmailVerified()
        }
    }

    fun updateProfile(user: User){
        repository.updateProfile(user)
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

        val baseUrl = "https://travellersystems.page.link/"
        val fullUrl = Uri.parse(baseUrl).buildUpon()
            .appendPath("verify")
            .appendQueryParameter("email", FirebaseAuth.getInstance().currentUser?.email)
            .build()

        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl(Uri.decode(fullUrl.toString()))
            .setAndroidPackageName("com.client.traveller", true, null)
            .setHandleCodeInApp(true)
            .build()

        user?.sendEmailVerification(actionCodeSettings)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.e(javaClass.simpleName, "sendEmailVerification successful")
                } else {
                    Log.e(javaClass.simpleName, task.exception?.localizedMessage)
                }
            }
    }


}