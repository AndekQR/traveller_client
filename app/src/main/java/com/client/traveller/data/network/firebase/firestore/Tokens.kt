package com.client.traveller.data.network.firebase.firestore

import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.firestore.model.Token
import com.client.traveller.ui.util.toLocalUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.suspendCoroutine

class Tokens {
    companion object {
        const val COLLECTION_NAME = "messaging_tokens"
    }

    private var token: Token? = null

    private fun getTokensCollection(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
    }

    fun saveToken(
        token: Token,
        user: User? = FirebaseAuth.getInstance().currentUser?.toLocalUser()
    ) {
        user?.let {
            this.token = token
            this.getTokensCollection().document(it.idUserFirebase!!).set(token)
        }
    }

    suspend fun getCurrentToken() = suspendCoroutine<Token?> { continuation ->
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
                continuation.resumeWith(Result.success(Token(currentUserUid, task.result?.token)))
            } else {
                continuation.resumeWith(Result.failure(task.exception!!))
            }
        }
    }

    suspend fun getUserToken(userIdFirebase: String) = withContext(Dispatchers.IO) {
        suspendCoroutine<Token> { continuation ->
            getTokensCollection().document(userIdFirebase).get().addOnSuccessListener {
                val token = it.toObject(Token::class.java)
                token?.let { continuation.resumeWith(Result.success(token)) }
            }
        }
    }
}