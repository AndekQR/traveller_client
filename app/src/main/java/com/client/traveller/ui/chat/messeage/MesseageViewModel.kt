package com.client.traveller.ui.chat.messeage

import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import com.client.traveller.data.db.entities.Messeage
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.firestore.model.ChatFirestoreModel
import com.client.traveller.data.repository.message.MessagingRepository
import com.client.traveller.data.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.Exception

class MesseageViewModel(
    private val userRepository: UserRepository,
    private val messagingRepository: MessagingRepository
) : ViewModel() {

    // TODO trzeba zapisywać wartości do lokalnej bazy
    // gdy użytkownik wybierze usera z listy userów tu będzie id tego usera
    // w przypadku wiadomości tak samo
    internal var userId: String? = null
    internal var chatId: String? = null
    internal var tripUid: String? = null

    private var _currentUser: MutableLiveData<User> = MutableLiveData()
    val currentUser: LiveData<User>
        get() = _currentUser
    private lateinit var currentUserObserver: Observer<User>

    // uczestnicy danego chatu (razem z aktualnym użytkownikiem)
    internal val chatParticipants = mutableSetOf<User>()

    init {
        this.initLiveData()
    }

    private fun initLiveData() = viewModelScope.launch(Dispatchers.Main) {
        currentUserObserver = Observer { user ->
            if (user == null) return@Observer
            _currentUser.value = user
        }
        userRepository.getUser().observeForever(currentUserObserver)
    }

    /**
     * Gdy wchodzimy w tą aktywność to przekazujemy albo userId albo chatID
     * Na podstawie jednej z tych wartości jest ustalane czy użytkownik wybrał użytkownika z listy użytkowników
     * czy wiadomość z listy wiadomości
     * Pobiera również uid wycieczki (bo wiadomości są w obrębie danej wycieczki)
     * To zadanie należy do tej metody
     *
     * @param intent dane przekazane tej aktywności podczas jej uruchamiania
     */
    suspend fun setIdentifier(intent: Intent) {
        val userId = intent.extras!!.getString("userId")
        val chatId = intent.extras!!.getString("chatId")
        this.tripUid = intent.extras!!.getString("tripUid")

        if (userId != null) {
            this.userId = userId
            val clickedUser = getUserByFirestoreId(userId)
            clickedUser.email?.let { addChatParticipantLocal(it) }

        } else if (chatId != null) {
            this.chatId = chatId
            val clickedChat = findChatByUid(this.chatId!!)
            this.addChatParticipantLocal(clickedChat.participantsUid?.keys)
        }
    }

    /**
     * Metoda dodaje użytkownika powiązanego z danym emailem do chatu do
     * aktualnego chatu
     */
    suspend fun addChatParticipantLocal(email: String?) {
        if (email == null) return
        val user = userRepository.getUsersByEmails(arrayListOf(email))
        user.forEach {
            this.chatParticipants.add(it)
        }
    }

    private suspend fun addChatParticipantLocal(uids: Set<String>?) {
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
        this.chatParticipants.mapTo(uidParticipants){
            it.idUserFirebase!!
        }
        return uidParticipants
    }

    suspend fun getUserByFirestoreId(id: String) = userRepository.getUserByFirestoreId(id)
    fun sendMesseageAsync(chatUid: String, messeage: Messeage) = messagingRepository.saveMesseage(chatUid, messeage)

    suspend fun findChat(participants: ArrayList<String>): ChatFirestoreModel? {
        return try {
            messagingRepository.findChat(participants, tripUid!!)
        } catch (ex: Exception) {
            Log.e(javaClass.simpleName, "catch")
            val result = messagingRepository.createChat(participants, this.tripUid!!)
            if (result) {
                try {
                    messagingRepository.findChat(participants, this.tripUid!!)
                }catch (ex: Exception){
                    throw ex
                }
            } else {
                null
            }
        }
    }

    suspend fun findChatByUid(uid: String) = this.messagingRepository.findChatByUid(uid)

    private fun removeObservers() = viewModelScope.launch(Dispatchers.IO) {
        userRepository.getUser().removeObserver(currentUserObserver)
    }

    override fun onCleared() {
        super.onCleared()

        this.removeObservers()
    }
}