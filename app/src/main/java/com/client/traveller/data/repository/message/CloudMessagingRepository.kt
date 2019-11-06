package com.client.traveller.data.repository.message

import androidx.lifecycle.LiveData
import com.client.traveller.data.db.entities.Messeage
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.firestore.model.ChatFirestoreModel
import java.util.ArrayList

interface CloudMessagingRepository {

    fun refreshToken()
    fun saveMesseage(chatUid: String, messeage: Messeage)
    fun getCurrentUserMesseagesAsSender(): LiveData<List<Messeage>>
    fun getCurrentUserMesseagesAsReceiver(): LiveData<List<Messeage>>
    suspend fun findChat(participants: ArrayList<String>, tripUid: String): ChatFirestoreModel
    suspend fun createChat(participants: ArrayList<String>, tripUid: String): Boolean
    fun getUsersChats(userId: String, tripUid: String): LiveData<List<ChatFirestoreModel>>
    suspend fun findChatByUid(uid: String): ChatFirestoreModel
}