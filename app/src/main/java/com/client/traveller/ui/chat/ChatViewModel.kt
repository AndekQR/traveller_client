package com.client.traveller.ui.chat

import androidx.lifecycle.*
import com.client.traveller.data.db.entities.Messeage
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.firestore.model.ChatFirestoreModel
import com.client.traveller.data.repository.map.MapRepository
import com.client.traveller.data.repository.message.MessagingRepository
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.user.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel(
    private val userRepository: UserRepository,
    private val messagingRepository: MessagingRepository,
    private val tripRepository: TripRepository,
    private val mapRepository: MapRepository
) : ViewModel() {

    var currentUser: LiveData<User> = userRepository.getCurrentUser()
    var currentTrip: LiveData<Trip> = tripRepository.getCurrentTrip()
    lateinit var currentUserChats: LiveData<List<ChatFirestoreModel>>
    lateinit var chatsLastMessage: LiveData<MutableMap<String, Messeage>>

    val searchQuery: MutableLiveData<String> = MutableLiveData()

    fun initUsersChats(userId: String, tripUid: String) {
        this.currentUserChats = this.messagingRepository.getUsersChats(userId, tripUid).asLiveData()
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
        messagingRepository.initChatsLastMessageRemoveObservers()
    }

    fun initChatsLastMessage(chatsUid: List<String>) {
        chatsUid.forEach {
            messagingRepository.initChatLastMesseage(chatUid = it)
        }
        this.chatsLastMessage = messagingRepository.getChatsLastMessage()
    }

    override fun onCleared() {
        super.onCleared()

        this.removeObservers()
    }


    fun startLocationUpdates() = mapRepository.startLocationUpdates()
    fun stopLocationUpdates() = mapRepository.stopLocationUpdates()
    fun sendingLocationData() = mapRepository.sendingLocationData()
}
