package com.client.traveller.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.repository.Repository
import com.client.traveller.ui.util.Coroutines
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseUser

class AuthViewModel(
    private val repository: Repository
) : ViewModel() {


    fun getLoggedInUser(): LiveData<User> {
        return repository.getUser()
    }

    fun logInUser(firebaseUser: FirebaseUser?) {
        Coroutines.io {
            if (firebaseUser != null) {
                repository.saveUser(firebaseUser)
            }
            return@io
        }
    }

    fun validate(email: String, password: String, displayName: String? = null, rePassword: String? = null): Boolean{
        var valid = true

        if (email.isEmpty() || password.isEmpty())
            valid = false

        if (displayName !=null  && rePassword != null){
            if (displayName.isEmpty() || rePassword.isEmpty())
                valid = false
            else if (password != rePassword)
                valid = false
        }
        else
            valid = false

        return valid
    }

    /**
     * Wysyła email weryfikacyjny do podanego użytkownika
     * metoda aktualizuje pole veriify w encji utkownika
     *
     * @param user użytkownik do którego zostanie wysłany email weryfikacyjny
     */
    //TODO zwalnianie domeny traveler.systems name.com
//    fun sendEmailVerification(user: FirebaseUser?){
//
//        val url = "https://traveller.systems/verify/"
//        val actionCodeSettings = ActionCodeSettings.newBuilder()
//            .setUrl(url)
//            .setAndroidPackageName("com.client.traveller", false, null)
//            .build()
//
//        user?.sendEmailVerification(actionCodeSettings)
//            ?.addOnCompleteListener{task ->
//                val result = task.result?.toString()
//                val exception = task.exception?.message
//                val asd = "asd"
//            }
//    }


}