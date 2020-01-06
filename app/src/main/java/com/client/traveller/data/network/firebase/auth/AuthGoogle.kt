package com.client.traveller.data.network.firebase.auth

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

object AuthGoogle {

    fun login(task: Task<GoogleSignInAccount>): Task<AuthResult>? {
        val account = handleSignInResult(task)
        if (account != null) {
            return this.firebaseAuthWithGoogle(account)
        }
        return null
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>): GoogleSignInAccount? {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                return account
            }
        } catch (ex: ApiException) {
            return null
        }
        return null
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount): Task<AuthResult> {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        return FirebaseAuth.getInstance().signInWithCredential(credential)
    }

    fun logout(googleSignInClient: GoogleSignInClient) {
        googleSignInClient.signOut()
    }
}