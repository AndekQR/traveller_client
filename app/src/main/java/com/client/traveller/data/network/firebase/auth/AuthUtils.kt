package com.client.traveller.data.network.firebase.auth

import android.net.Uri
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class AuthUtils {

    fun sendEmailVerification(user: FirebaseUser): Task<Void> {
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

        return user.sendEmailVerification(actionCodeSettings)
    }

    fun updateFirebaseUserProfile(
        user: FirebaseUser,
        profileUpdates: UserProfileChangeRequest
    ): Task<Void> {
        return user.updateProfile(profileUpdates)
    }

}