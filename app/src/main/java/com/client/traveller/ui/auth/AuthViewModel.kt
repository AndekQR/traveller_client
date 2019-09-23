package com.client.traveller.ui.auth

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.repository.Repository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlin.math.log

class AuthViewModel(
    private val repository: Repository
) : ViewModel() {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getLoggedInUser(): LiveData<User> {
        return repository.getUser()
    }

    fun logInEmailUser(firebaseUser: FirebaseUser, displayName: String? = null) {
        val user = User(
            firebaseUser.uid,
            displayName ?: firebaseUser.displayName,
            firebaseUser.email,
            firebaseUser.isEmailVerified
        )

        repository.saveUser(user)
    }

    fun logInGoogleUser(googleUser: GoogleSignInAccount){
        val user = User(
            googleUser.id,
            googleUser.displayName,
            googleUser.email,
            true
        )

        repository.saveUser(user)
    }


    fun validate(
        email: String,
        password: String,
        displayName: String? = null,
        rePassword: String? = null
    ): Boolean {
        var valid = true

        // logowanie i rejestracja
        if (email.isEmpty() || password.isEmpty())
            valid = false

        // rejestracja
        if (displayName != null && rePassword != null) {
            if (displayName.isEmpty() || rePassword.isEmpty())
                valid = false
            else if (password != rePassword)
                valid = false
        }

        return valid
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