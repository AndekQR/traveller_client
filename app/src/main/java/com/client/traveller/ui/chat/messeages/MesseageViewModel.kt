package com.client.traveller.ui.chat.messeages

import android.content.Intent
import androidx.lifecycle.*
import com.client.traveller.data.db.entities.Messeage
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.firestore.model.ChatFirestoreModel
import com.client.traveller.data.repository.message.MessagingRepository
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.user.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest

class MesseageViewModel(
    private val userRepository: UserRepository,
    private val messagingRepository: MessagingRepository,
    private val tripRepository: TripRepository
) : ViewModel() {

    // TODO trzeba zapisywać wartości do lokalnej bazy
    // gdy użytkownik wybierze usera z listy userów tu będzie id tego usera
    // w przypadku wiadomości tak samo
    internal var userId: String? = null
    internal var chatId: String? = null

    val currentUser: LiveData<User> = userRepository.getCurrentUser()
    val currentTrip: LiveData<Trip> = tripRepository.getCurrentTrip()
    lateinit var chatMesseages: LiveData<List<Messeage>>
    // uczestnicy danego chatu (razem z aktualnym użytkownikiem)
    internal val chatParticipants = mutableSetOf<User>()

    /**
     * Gdy wchodzimy w tą aktywność to przekazujemy albo userId albo chatID
     * Na podstawie jednej z tych wartości jest ustalane czy użytkownik wybrał użytkownika z listy użytkowników
     * czy wiadomość z listy wiadomości
     * Pobiera również uid wycieczki (bo wiadomości są w obrębie danej wycieczki)
     *
     * Jeżeli zwróci nie nulla to aktywność została uruchomiona z powiadomienia o wiadomości
     * wtedy zwróci tripUid przekazanego jako chatId czatu
     *
     * @param intent dane przekazane tej aktywności podczas jej uruchamiania
     */
    suspend fun identifyChat(intent: Intent, currentUserEmail: String?) {
        val userId = intent.extras!!.getString("userId")
        val chatId = intent.extras!!.getString("chatId")

        if (userId != null) {
            this.userId = userId
            val clickedUser = getUserByFirestoreId(userId)
            clickedUser.email?.let { addChatParticipantsLocal(it) }
            currentUserEmail?.let { addChatParticipantsLocal(it) }

        } else if (chatId != null) {
            this.chatId = chatId
            val clickedChat = findChatByUid(this.chatId!!)
            this.addChatParticipantsLocal(clickedChat.participantsUid?.keys)
        }
    }

    /**
     * Metoda dodaje użytkownika powiązanego z danym emailem do chatu do
     * aktualnego chatu
     */
    suspend fun addChatParticipantsLocal(email: String?) {
        if (email == null) return
        val user = userRepository.getUsersByEmails(arrayListOf(email))
        user.forEach {
            this.chatParticipants.add(it)
        }
    }

    private suspend fun addChatParticipantsLocal(uids: Set<String>?) {
        uids?.forEach {
            val user = userRepository.getUserByFirestoreId(it)
            this.chatParticipants.add(user)
        }
    }

    /**
     * zwraca listę uidFirebase uczestników aktualnego chatu
     */
    fun getChatParticipantsUid(): ArrayList<String> {
        val uidParticipants = arrayListOf<String>()
        this.chatParticipants.mapTo(uidParticipants) {
            it.idUserFirebase!!
        }
        return uidParticipants
    }

    suspend fun findTripByUid(tripUid: String) = tripRepository.getTripByUid(tripUid)

    suspend fun getUserByFirestoreId(id: String) = userRepository.getUserByFirestoreId(id)
    suspend fun sendMesseage(chat: ChatFirestoreModel, messeage: Messeage) =
        messagingRepository.saveMesseage(chat, messeage)

    suspend fun findChat(participants: ArrayList<String>, tripUid: String): ChatFirestoreModel? {
        return try {
            messagingRepository.findChat(participants, tripUid)
        } catch (ex: Exception) {
            val result = messagingRepository.createChat(participants, tripUid)
            if (result) {
                try {
                    messagingRepository.findChat(participants, tripUid)
                } catch (ex: Exception) {
                    throw ex
                }
            } else {
                null
            }
        }
    }

    suspend fun findChatByUid(uid: String) = this.messagingRepository.findChatByUid(uid)


    @ExperimentalCoroutinesApi
    fun initChatMesseages() {
        this.chatMesseages = liveData {
            messagingRepository.initChatMessages(chatId!!).collectLatest {
                emit(it)
            }
        }
    }
}