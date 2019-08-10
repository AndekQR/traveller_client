package com.client.traveller.ui.auth

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.repository.UserRepository
import com.client.traveller.ui.util.ApiException
import com.client.traveller.ui.util.Coroutines
import com.client.traveller.ui.util.NoInternetAvailableException

class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    var email: String? = null
    var password: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var confirmPassword: String? = null

    var authListener: AuthListener? = null

    fun getLoggedInUser(): LiveData<User> {
        return userRepository.getUser()
    }

    fun onLoginButtonClick(v: View) {
        authListener?.onStarted()

        if (!isEmailValid(email) || password.isNullOrEmpty()) {
            authListener?.onFailure("Invalid email or password")
            return
        }

        Coroutines.main {
            try {
                val loginResponse = userRepository.login(email?.trim()!!, password?.trim()!!)
                loginResponse.user?.let {
                    authListener?.onSuccess(it)
                    userRepository.saveUser(it)
                    return@main
                }
                authListener?.onFailure("Not logged<--")//e.message
            } catch (e: ApiException) {
                authListener?.onFailure(e.message!!)
            } catch (e: NoInternetAvailableException) {
                authListener?.onFailure(e.message!!)
            }
        }
    }

    fun toLogin(v: View) {
        Intent(v.context, LoginActivity::class.java).also {
            it.addFlags(FLAG_ACTIVITY_NEW_TASK)
            it.addFlags(FLAG_ACTIVITY_CLEAR_TASK)
            v.context.startActivity(it)
        }
    }

    fun toRegister(v: View) {
        Intent(v.context, SignupActivity::class.java).also {
//            it.addFlags(FLAG_ACTIVITY_NEW_TASK)
//            it.addFlags(FLAG_ACTIVITY_CLEAR_TASK)
            v.context.startActivity(it)
        }
    }

    fun onSignupButtonClick(v: View) {
        authListener?.onStarted()

        if (firstName.isNullOrEmpty()) {
            authListener?.onFailure("Name is required")
            return
        }

        if (lastName.isNullOrEmpty()) {
            authListener?.onFailure("Last name is required")
            return
        }

        if (!isEmailValid(email)) {
            authListener?.onFailure("No email or it is not correct")
            return
        }

        if (password.isNullOrEmpty()) {
            authListener?.onFailure("Please enter a password")
            return
        }

        if (password != confirmPassword) {
            authListener?.onFailure("Password did not match")
            return
        }


        Coroutines.main {
            try {
                val authResponse = userRepository.register(firstName?.trim()!!, lastName?.trim()!!, email?.trim()!!, password?.trim()!!)
                authResponse.user?.let {
                    authListener?.onSuccess(it)
                    userRepository.saveUser(it)
                    return@main
                }
                authListener?.onFailure("Not logged<---")
            } catch (e: ApiException) {
                authListener?.onFailure(e.message!!)
            } catch (e: NoInternetAvailableException) {
                authListener?.onFailure(e.message!!)
            }
        }
    }

    private fun isEmailValid(email: String?): Boolean{
        return if (email.isNullOrEmpty()) false
        else android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}