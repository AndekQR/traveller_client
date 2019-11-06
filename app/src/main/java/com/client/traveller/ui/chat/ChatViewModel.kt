package com.client.traveller.ui.chat

import androidx.collection.ArraySet
import androidx.collection.arraySetOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.client.traveller.data.db.entities.Messeage
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.firestore.model.ChatFirestoreModel
import com.client.traveller.data.repository.message.CloudMessagingRepository
import com.client.traveller.data.repository.trip.TripRepository
import com.client.traveller.data.repository.user.UserRepository
import com.client.traveller.ui.util.CombinedLiveData
import com.client.traveller.ui.util.lazyDeferred
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class ChatViewModel(
    private val userRepository: UserRepository,
    private val cloudMessagingRepository: CloudMessagingRepository,
    private val tripRepository: TripRepository
) : ViewModel() {

    // wszystkie wycieczki pobierane z firestore
    private val _usersTrip = MutableLiveData<List<User>>()
    internal val usersTrip: LiveData<List<User>> = _usersTrip

    // aktualna wycieczka użytkownika
    internal val currentTrip = tripRepository.getCurrentTrip()
    internal val currentUser = userRepository.getUser()

    // zawiera liste z wiadomościami użytkownika, zarówno jako wysylający jak i odbierający
    internal var currentUserMesseages = MediatorLiveData<List<Messeage>>()

    internal lateinit var currentUserChats: LiveData<List<ChatFirestoreModel>>

    private val combine = fun(data1: List<Messeage>?, data2: List<Messeage>?): List<Messeage> {
        val result: Set<Messeage> = ArraySet(data1) + ArraySet(data2)
        return result.toList()
    }

    init {

        this.currentUserMesseages = CombinedLiveData(
            cloudMessagingRepository.getCurrentUserMesseagesAsReceiver(),
            cloudMessagingRepository.getCurrentUserMesseagesAsSender(),
            combine
        )
    }

    // TODO gdy ktoś napisze do mnie do chat się nie zaktualizuje?
    fun initUsersChats(userId: String, tripUid: String) {
        this.currentUserChats = this.cloudMessagingRepository.getUsersChats(userId, tripUid)
    }

    fun logoutUser(mGoogleSignInClient: GoogleSignInClient) =
        userRepository.logoutUser(mGoogleSignInClient)

    suspend fun refreshUsers(emails: ArrayList<String>?) {
        if (emails != null) {
            val filteredEmails = ArrayList(emails.filter { it.isNotBlank() })
            _usersTrip.value = userRepository.getUsersByEmails(filteredEmails)
        }
    }

     suspend fun getUsersById(ids: ArrayList<String>) = this.userRepository.getUsersByIds(ids)
}
