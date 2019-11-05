package com.client.traveller.data.repository.message

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.client.traveller.data.db.entities.Messeage
import com.client.traveller.data.network.firebase.firestore.Chats
import com.client.traveller.data.network.firebase.firestore.Messeages
import com.client.traveller.data.network.firebase.firestore.Tokens
import com.client.traveller.data.network.firebase.firestore.Users
import com.client.traveller.data.network.firebase.firestore.model.ChatFirestoreModel
import com.client.traveller.data.network.firebase.messaging.CloudMessaging
import com.client.traveller.ui.util.Coroutines.io
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CloudMessagingRepositoryImpl(
    private val tokens: Tokens,
    private val cloudMessaging: CloudMessaging,
    private val messeages: Messeages,
    private val users: Users,
    private val chats: Chats
) : CloudMessagingRepository {

    private var currentUser: FirebaseUser? = null

    private val _userMesseagesAsSender = MutableLiveData<List<Messeage>>()
    private val userMesseagesAsSender: LiveData<List<Messeage>>
        get() = _userMesseagesAsSender

    private val _userMesseagesAsReceiver = MutableLiveData<List<Messeage>>()
    private val userMesseagesAsReceiver: LiveData<List<Messeage>>
        get() = _userMesseagesAsReceiver

    init {
        this.currentUser = FirebaseAuth.getInstance().currentUser
        this.currentUser?.let {
            //            this.initializeCurrentUserMesseagesAsSender()
//            this.initializeCurrentUserMesseagesAsReceiver()
        }
    }

    override fun refreshToken() {
        io {
            val token = this.tokens.getCurrentToken()
            token?.let { this.tokens.saveToken(it) }
        }
    }

    override fun saveMesseage(chatUid: String, messeage: Messeage) =
        this.messeages.saveMesseage(chatUid, messeage)

//    private fun initializeCurrentUserMesseagesAsSender() {
//        this.messeages.getUserMesseagesAsSender(currentUser?.uid!!)
//            .addSnapshotListener(EventListener<QuerySnapshot> { querySnapshot, exception ->
//                exception?.let {
//                    return@EventListener
//                }
//
//                val messeages = mutableListOf<Messeage>()
//                querySnapshot?.forEach { doc ->
//                    val messeage = doc.toObject(Messeage::class.java)
//                    messeages.add(messeage)
//                }
//                this._userMesseagesAsSender.value = messeages
//            })
//    }
//
//    private fun initializeCurrentUserMesseagesAsReceiver() {
//        this.messeages.getUserMesseagesAsReceiver(currentUser?.uid!!)
//            .addSnapshotListener(EventListener<QuerySnapshot> { querySnapshot, exception ->
//                exception?.let {
//                    return@EventListener
//                }
//
//                val messeages = mutableListOf<Messeage>()
//                querySnapshot?.forEach { doc ->
//                    val messeage = doc.toObject(Messeage::class.java)
//                    messeages.add(messeage)
//                }
//                this._userMesseagesAsReceiver.value = messeages
//            })
//    }

    override fun getCurrentUserMesseagesAsSender() = this.userMesseagesAsSender
    override fun getCurrentUserMesseagesAsReceiver() = this.userMesseagesAsReceiver

    override suspend fun createChat(participants: ArrayList<String>): Boolean {
        val participantsMap = participants.map {
           it to true
        }.toMap()
        val chat = ChatFirestoreModel(participantsUid = participantsMap, participantsNumber = participantsMap.size)
        return suspendCoroutine { continuation ->
            chats.addChat(chat).addOnCompleteListener {task ->
                if (task.isSuccessful) continuation.resumeWith(Result.success(true))
                else continuation.resumeWith(Result.success(false))
            }
        }
    }


    override suspend fun findChat(participants: ArrayList<String>) = withContext(Dispatchers.IO) {
        suspendCoroutine<ChatFirestoreModel> { continuation ->
            chats.getChatByParticipants(participants).get().addOnSuccessListener { query ->
                    if (query.size() > 0){
                        val chat = query.first().toObject(ChatFirestoreModel::class.java) as ChatFirestoreModel?
                        chat?.let { continuation.resumeWith(Result.success(it)) }
                    } else {
                        continuation.resumeWithException(NoSuchElementException())
                    }
            }
        }
    }

}