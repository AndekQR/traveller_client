package com.client.traveller.data.repository.user

import androidx.lifecycle.LiveData
import com.client.traveller.data.db.entities.User
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

interface UserRepository {
    fun registerUser(user: User, function: (Boolean, Exception?) -> Unit)
    fun loginUser(username: String, password: String, function: (Boolean, Exception?) -> Unit)
    fun getUser(): LiveData<User>
    fun logoutUser(googleSignInClient: GoogleSignInClient) //jest tylko jeden aktualnie zalogowany!
    fun setEmailVerifiedAsync()
    fun updateProfile(user: User)
    fun loginGoogleUser(task: Task<GoogleSignInAccount>?, function: (Boolean, Exception?) -> Unit)
    fun loginFacebookUser(accessToken: AccessToken, function: (Boolean, Exception?) -> Unit)
    fun resetPasswordSendEmail(email: String, function: (Boolean, Exception?) -> Unit)
    fun createUserEmailPassword(
        email: String,
        password: String,
        displayName: String,
        function: (Boolean, Exception?, FirebaseUser?) -> Unit
    )

    fun updateProfile(
        user: FirebaseUser,
        profileChangeRequest: UserProfileChangeRequest,
        function: (Boolean, Exception?) -> Unit
    )

    fun sendEmailVerification(user: FirebaseUser, function: (Boolean, Exception?) -> Unit)
    fun updateLocalUserDataAsync(user: User)
    fun updateAvatar(user: User, imageUri: String)

    suspend fun getUsersByEmails(emails: ArrayList<String>): List<User>
}