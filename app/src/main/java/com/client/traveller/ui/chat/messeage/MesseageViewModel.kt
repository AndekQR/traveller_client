package com.client.traveller.ui.chat.messeage

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.repository.message.CloudMessagingRepository
import com.client.traveller.data.repository.user.UserRepository

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

    suspend fun addChatParticipant(email: String) {
        val user = userRepository.getUsersByEmails(arrayListOf(email))
        user.forEach {
            this.chatParticipants.add(it)
        }
    }

    suspend fun getUserByFirestoreId(id: String) = userRepository.getUserByFirestoreId(id)

}