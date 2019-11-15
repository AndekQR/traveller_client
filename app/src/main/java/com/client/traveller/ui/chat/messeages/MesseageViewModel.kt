package com.client.traveller.ui.chat.messeages

import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import com.client.traveller.data.db.entities.Messeage
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.firestore.model.ChatFirestoreModel
import com.client.traveller.data.repository.message.MessagingRepository
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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


    private var _currentUser: MutableLiveData<User> = MutableLiveData()
    val currentUser: LiveData<User>
        get() = _currentUser
    private lateinit var currentUserObserver: Observer<User>

    private val _chatMesseages = MutableLiveData<List<Messeage>>()
    val chatMesseages: LiveData<List<Messeage>>
        get() = _chatMesseages
    private lateinit var chatMesseagesObserver: Observer<List<Messeage>>

    private var _currentTrip: MutableLiveData<Trip> = MutableLiveData()
    val currentTrip: LiveData<Trip>
        get() = _currentTrip
    private lateinit var currentTripObserver: Observer<Trip>

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

        currentTripObserver = Observer { trip ->
            _currentTrip.value = trip
        }
        tripRepository.getCurrentTrip().observeForever(currentTripObserver)
    }

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
    suspend fun setIdentifier(intent: Intent): String? {
        val userId = intent.extras!!.getString("userId")
        val chatId = intent.extras!!.getString("chatId")

        val tripUid = intent.extras!!.getString("tripUid")

        if (userId != null) {
            this.userId = userId
            val clickedUser = getUserByFirestoreId(userId)
            clickedUser.email?.let { addChatParticipantLocal(it) }

        } else if (chatId != null) {
            this.chatId = chatId
            val clickedChat = findChatByUid(this.chatId!!)
            this.addChatParticipantLocal(clickedChat.participantsUid?.keys)
        }
        return tripUid
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


    fun initChatMesseages() {
        this.chatMesseagesObserver = Observer { messeages ->
            if (messeages == null) return@Observer
            this._chatMesseages.value = messeages
        }
        try {
            this.messagingRepository.initMesseages(this.chatId!!)
                .observeForever(this.chatMesseagesObserver)
        } catch (ex: NullPointerException) {
            Log.e(javaClass.simpleName, "Wiadomości nie zostały zainicjalizowane, tripUid == null")
        }
    }

    fun removeChatMesseagesObserver() {
        if (::chatMesseagesObserver.isInitialized)
            this.messagingRepository.chatMesseagesRemoveObserver(this.chatMesseagesObserver)
    }

    private fun removeObservers() = viewModelScope.launch(Dispatchers.IO) {
        userRepository.getUser().removeObserver(currentUserObserver)
        tripRepository.getCurrentTrip().removeObserver(currentTripObserver)
    }

    override fun onCleared() {
        super.onCleared()

        this.removeObservers()
    }
}