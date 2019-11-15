package com.client.traveller.data.repository.message

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.client.traveller.data.db.entities.Messeage
import com.client.traveller.data.network.firebase.firestore.model.ChatFirestoreModel
import com.client.traveller.data.network.firebase.firestore.model.Token
import java.util.*
import kotlin.collections.ArrayList

interface MessagingRepository {

    suspend fun refreshToken(): Unit?
    suspend fun saveMesseage(chat: ChatFirestoreModel, messeage: Messeage)
    fun initMesseages(chatUid: String): LiveData<List<Messeage>>
    suspend fun findChat(participants: ArrayList<String>, tripUid: String): ChatFirestoreModel
    suspend fun createChat(participants: ArrayList<String>, tripUid: String): Boolean
    fun getUsersChats(userId: String, tripUid: String): LiveData<List<ChatFirestoreModel>>
    suspend fun findChatByUid(uid: String): ChatFirestoreModel
    fun getUsersChatsRemoveObserver(observer: Observer<List<ChatFirestoreModel>>)
    suspend fun getUsersTokens(userIds: List<String>?): List<Token>?
    fun chatMesseagesRemoveObserver(observer: Observer<List<Messeage>>)
    fun initChatLastMesseage(chatUid: String)
    fun getChatsLastMessage(): LiveData<MutableMap<String, Messeage>>
    fun initChatsLastMessageRemoveObservers()
}