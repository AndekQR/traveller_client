package com.client.traveller.ui.chat

import androidx.lifecycle.ViewModel
import com.client.traveller.data.repository.message.CloudMessagingRepository
import com.client.traveller.data.repository.user.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class ChatViewModel(
    private val userRepository: UserRepository,
    private val cloudMessagingRepository: CloudMessagingRepository
) : ViewModel() {

    fun logoutUser(mGoogleSignInClient: GoogleSignInClient) = userRepository.logoutUser(mGoogleSignInClient)
}
