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
import com.client.traveller.ui.util.randomUid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MessagingRepositoryImpl(
    private val tokens: Tokens,
    private val cloudMessaging: CloudMessaging,
    private val messeages: Messeages,
    private val users: Users,
    private val chats: Chats
) : MessagingRepository {

    private var currentUser: FirebaseUser? = null

    private val _usersChats = MutableLiveData<List<ChatFirestoreModel>>()
    private val usersChats: LiveData<List<ChatFirestoreModel>>
        get() = _usersChats

    private val _chatMesseages = MutableLiveData<List<Messeage>>()
    private val chatMesseages: LiveData<List<Messeage>>
        get() = _chatMesseages



    override fun refreshToken() {
        io {
            val token = this.tokens.getCurrentToken()
            token?.let { this.tokens.saveToken(it) }
        }
    }

    override fun saveMesseage(chatUid: String, messeage: Messeage) =
        this.messeages.saveMesseage(chatUid, messeage)




    override suspend fun createChat(participants: ArrayList<String>, tripUid: String): Boolean {
        val participantsMap = participants.map {
            it to true
        }.toMap()
        val chat = ChatFirestoreModel(
            participantsUid = participantsMap,
            participantsNumber = participantsMap.size,
            tripUid = tripUid,
            uid = randomUid()
        )
        return suspendCoroutine { continuation ->
            chats.addChat(chat).addOnCompleteListener { task ->
                if (task.isSuccessful) continuation.resumeWith(Result.success(true))
                else continuation.resumeWith(Result.success(false))
            }
        }
    }


    override suspend fun findChat(participants: ArrayList<String>, tripUid: String) =
        withContext(Dispatchers.IO) {
            suspendCoroutine<ChatFirestoreModel> { continuation ->
                chats.getChatByParticipantsFiltrSize(participants, tripUid).get()
                    .addOnSuccessListener { query ->
                        if (query.size() > 0) {
                            val chat =
                                query.first().toObject(ChatFirestoreModel::class.java) as ChatFirestoreModel?
                            chat?.let { continuation.resumeWith(Result.success(it)) }
                        } else {
                            continuation.resumeWithException(NoSuchElementException())
                        }
                    }
            }
        }

    /**
     * pobiera chaty danego użytkownika w obrębie danej wycieczki
     */
    override fun getUsersChats(
        userId: String,
        tripUid: String
    ): LiveData<List<ChatFirestoreModel>> {
        this.chats.getUserAllChats(userId, tripUid)
            .addSnapshotListener(EventListener<QuerySnapshot> { querySnapshot, exception ->
                exception?.let {
                    return@EventListener
                }

                val chats = mutableListOf<ChatFirestoreModel>()
                querySnapshot?.forEach {doc ->
                    val chat = doc.toObject(ChatFirestoreModel::class.java)
                    chats.add(chat)
                }
                _usersChats.value = chats
            })
        return usersChats
    }

    override suspend fun findChatByUid(uid: String) = withContext(Dispatchers.IO) {
        suspendCoroutine<ChatFirestoreModel> {continuation ->
            chats.getChatByUid(uid).get().addOnSuccessListener {snapshot ->
                snapshot?.let {
                    if (it.size() == 1){
                        val chat = it.first().toObject(ChatFirestoreModel::class.java)
                        continuation.resumeWith(Result.success(chat))
                    }
                }
            }
        }
    }

    override fun getMesseages(chatUid: String): LiveData<List<Messeage>> {
        val asd: LiveData<List<Messeage>> = MutableLiveData(listOf(Messeage()))
        return asd
    }
}