package com.client.traveller.data.repository.message

import android.util.Log
import com.client.traveller.data.network.firebase.firestore.Tokens
import com.client.traveller.data.network.firebase.messaging.CloudMessaging
import com.client.traveller.ui.util.Coroutines.io

class CloudMessagingRepositoryImpl(
    private val tokens: Tokens,
    private val cloudMessaging: CloudMessaging
) : CloudMessagingRepository {

    override fun refreshToken() {
        io {
            val token = this.tokens.getCurrentToken()
            Log.e(javaClass.simpleName, token ?: "token null")
            token?.let { this.tokens.saveToken(it) }
        }
    }
}