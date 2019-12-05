package com.client.traveller.data.repository.message

import androidx.lifecycle.LiveData
import com.client.traveller.data.db.entities.Messeage
import com.client.traveller.data.network.firebase.firestore.model.ChatFirestoreModel
import com.client.traveller.data.network.firebase.firestore.model.Token
import kotlinx.coroutines.flow.Flow

interface MessagingRepository {

    suspend fun refreshToken(): Unit?
    suspend fun saveMesseage(chat: ChatFirestoreModel, messeage: Messeage)
    suspend fun findChat(participants: ArrayList<String>, tripUid: String): ChatFirestoreModel
    suspend fun createChat(participants: ArrayList<String>, tripUid: String): Boolean
    fun getUsersChats(userId: String, tripUid: String): Flow<List<ChatFirestoreModel>>
    suspend fun findChatByUid(uid: String): ChatFirestoreModel
    suspend fun getUsersTokens(userIds: List<String>?): List<Token>?
    fun initChatLastMesseage(chatUid: String)
    fun getChatsLastMessage(): LiveData<MutableMap<String, Messeage>>
    fun initChatsLastMessageRemoveObservers()
    fun initChatMessages(chatUid: String): Flow<List<Messeage>>

}