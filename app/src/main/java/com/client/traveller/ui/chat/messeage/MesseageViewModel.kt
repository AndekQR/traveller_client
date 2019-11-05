package com.client.traveller.ui.chat.messeage

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import com.client.traveller.data.db.entities.Messeage
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.firestore.model.ChatFirestoreModel
import com.client.traveller.data.repository.message.CloudMessagingRepository
import com.client.traveller.data.repository.user.UserRepository
import java.util.NoSuchElementException
import kotlin.Exception

class MesseageViewModel(
    private val userRepository: UserRepository,
    private val cloudMessagingRepository: CloudMessagingRepository
) : ViewModel() {

    // TODO trzeba zapisywać wartości do lokalnej bazy i firestore
    // gdy użytkownik wybierze usera z listy userów tu będzie id tego usera
    // w przypadku wiadomości tak samo
    internal var userId: String? = null
    internal var chatId: String? = null

    internal val currentUser = userRepository.getUser()
    internal val chatParticipants = mutableSetOf<User>()

    suspend fun setIdentifier(intent: Intent) {
        val userId = intent.extras!!.getString("userId")
        val chatId = intent.extras!!.getString("chatId")
        if (userId != null) {
            this.userId = userId
            val clickedUser = getUserByFirestoreId(userId)
            clickedUser.email?.let { addChatParticipant(it) }

        } else if (chatId != null)
            this.chatId = chatId
    }

    suspend fun addChatParticipant(email: String?) {
        if (email == null) return
        val user = userRepository.getUsersByEmails(arrayListOf(email))
        user.forEach {
            this.chatParticipants.add(it)
        }
    }

    fun getChatParticipantsUid(): ArrayList<String> {
        val uidParticipants = arrayListOf<String>()
        this.chatParticipants.mapTo(uidParticipants){
            it.idUserFirebase!!
        }
        return uidParticipants
    }

    suspend fun getUserByFirestoreId(id: String) = userRepository.getUserByFirestoreId(id)
    fun sendMesseageAsync(chatUid: String, messeage: Messeage) = cloudMessagingRepository.saveMesseage(chatUid, messeage)

    suspend fun findChat(participants: ArrayList<String>): ChatFirestoreModel? {
        return try {
            cloudMessagingRepository.findChat(participants)
        } catch (ex: Exception) {
            Log.e(javaClass.simpleName, "catch")
            val result = cloudMessagingRepository.createChat(participants)
            if (result) {
                try {
                    cloudMessagingRepository.findChat(participants)
                }catch (ex: Exception){
                    throw ex
                }
            } else {
                null
            }
        }
    }

}