package com.client.traveller.ui.auth

import android.util.Log
import androidx.lifecycle.*
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.repository.user.UserRepository
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    val currentUser: LiveData<User> = this.userRepository.getCurrentUser()

    fun loginJustLocalGoogle(
        googleUser: GoogleSignInAccount
    ) {
        val user = User(
            googleUser.id,
            googleUser.displayName,
            googleUser.email,
            true,
            googleUser.photoUrl.toString()
        )

        userRepository.updateLocalUserDataAsync(user)
    }

    fun loginUser(username: String, password: String, function: (Boolean, Exception?) -> Unit) {
        userRepository.loginUser(username, password, function)
    }

    fun createUserNormal(
        email: String,
        password: String,
        displayName: String,
        function: (Boolean, Exception?, FirebaseUser?) -> Unit
    ) {
        userRepository.createUserEmailPassword(email, password, displayName, function)
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
    fun sendEmailVerification(user: FirebaseUser) {

        userRepository.sendEmailVerification(user) { isSuccessful, exception ->
            if (!isSuccessful) {
                Log.e(javaClass.simpleName, exception?.message)
            }
        }
    }

    fun loginUserByGoogle(
        task: Task<GoogleSignInAccount>?,
        function: (Boolean, Exception?) -> Unit
    ) {
        userRepository.loginGoogleUser(task, function)
    }

    fun loginUserByFacebook(
        accessToken: AccessToken,
        function: (Boolean, Exception?) -> Unit
    ) {
        userRepository.loginFacebookUser(accessToken, function)
    }

    fun resetPassword(
        email: String,
        function: (Boolean, Exception?) -> Unit
    ) {
        userRepository.resetPasswordSendEmail(email, function)
    }

    /**
     * Aktualizacja usera w firebase
     *
     * @param user użytkownik do aktualizacji
     * @param profileUpdates zaktualizowane dane
     */
    fun updateProfile(
        user: FirebaseUser,
        profileChangeRequest: UserProfileChangeRequest,
        function: (Boolean, Exception?) -> Unit
    ) {
        userRepository.updateProfile(user, profileChangeRequest, function)
    }

}