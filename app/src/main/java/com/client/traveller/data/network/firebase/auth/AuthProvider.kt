package com.client.traveller.data.network.firebase.auth

import com.google.firebase.auth.FirebaseUser

class AuthProvider {

    fun isUserGoogleAuth(user: FirebaseUser): Boolean {
        return user.providerData.any { userInfo ->
            userInfo.providerId == "google.com"
        }
    }

    fun isUserFacebookAuth(user: FirebaseUser): Boolean {
        return user.providerData.any { userInfo ->
            userInfo.providerId == "facebook.com"
        }
    }

    fun isUserNormalAuth(user: FirebaseUser): Boolean {
        return user.providerData.any { userInfo ->
            userInfo.providerId == "password"
        }
    }
}