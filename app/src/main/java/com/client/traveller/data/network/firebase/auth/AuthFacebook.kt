package com.client.traveller.data.network.firebase.auth

import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth

object AuthFacebook {

    fun login(accessToken: AccessToken): Task<AuthResult> {
        return this.handleFacebookAccessToken(accessToken)
    }

    /**
     * Metoda na podstawie tokena pobiera z facebooka dane o użytkowniku
     * i wykorzystuje je do utworzenia konta w firebase
     *
     * @param accessToken token użytkownika facebooka
     */
    private fun handleFacebookAccessToken(accessToken: AccessToken): Task<AuthResult> {
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        return FirebaseAuth.getInstance().signInWithCredential(credential)
    }

    fun logout() {
        LoginManager.getInstance().logOut()
    }
}