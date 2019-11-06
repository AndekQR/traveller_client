package com.client.traveller.ui.chat

import androidx.lifecycle.*
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.firestore.model.ChatFirestoreModel
import com.client.traveller.data.repository.message.MessagingRepository
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.user.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel(
    private val userRepository: UserRepository,
    private val messagingRepository: MessagingRepository,
    private val tripRepository: TripRepository
) : ViewModel() {

    private var _currentUser: MutableLiveData<User> = MutableLiveData()
    val currentUser: LiveData<User>
        get() = _currentUser
    private lateinit var currentUserObserver: Observer<User>

    private var _currentTrip: MutableLiveData<Trip> = MutableLiveData()
    val currentTrip: LiveData<Trip>
        get() = _currentTrip
    private lateinit var currentTripObserver: Observer<Trip>

    private var _currentUserChats: MutableLiveData<List<ChatFirestoreModel>> = MutableLiveData()
    val currentUserChats: LiveData<List<ChatFirestoreModel>>
        get() = _currentUserChats
    // Obserwator jest usuwany w ChatListFragment bo potrzebne są parametry
    private lateinit var currentUserChatsObserver: Observer<List<ChatFirestoreModel>>

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
            if (trip == null) return@Observer
            _currentTrip.value = trip
        }
        tripRepository.getCurrentTrip().observeForever(currentTripObserver)
    }

    fun initUsersChats(userId: String, tripUid: String) {
        currentUserChatsObserver = Observer { chats ->
            if (chats == null) return@Observer
            _currentUserChats.value = chats
        }
       this.messagingRepository.getUsersChats(userId, tripUid).observeForever(currentUserChatsObserver)
    }

    fun usersChatsRemoveLiveDataObserver(userId: String, tripUid: String) {
        this.messagingRepository.getUsersChats(userId, tripUid).removeObserver(currentUserChatsObserver)
    }

    fun logoutUser(mGoogleSignInClient: GoogleSignInClient) =
        userRepository.logoutUser(mGoogleSignInClient)

    suspend fun getUsersByEmails(emails: ArrayList<String>?): List<User>? {
        if (emails != null) {
            val filteredEmails = ArrayList(emails.filter { it.isNotBlank() })
            return this.userRepository.getUsersByEmails(filteredEmails)
        }
        return null
    }

     suspend fun getUsersById(ids: ArrayList<String>) = this.userRepository.getUsersByIds(ids)

    private fun removeObservers() = viewModelScope.launch(Dispatchers.IO) {
        userRepository.getUser().removeObserver(currentUserObserver)
        tripRepository.getCurrentTrip().removeObserver(currentTripObserver)
    }

    override fun onCleared() {
        super.onCleared()

        this.removeObservers()
    }
}
