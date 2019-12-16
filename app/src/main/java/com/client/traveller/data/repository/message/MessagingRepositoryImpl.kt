package com.client.traveller.data.repository.message

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.client.traveller.R
import com.client.traveller.data.db.MesseageDao
import com.client.traveller.data.db.entities.Messeage
import com.client.traveller.data.network.firebase.firestore.Chats
import com.client.traveller.data.network.firebase.firestore.Messeages
import com.client.traveller.data.network.firebase.firestore.Tokens
import com.client.traveller.data.network.firebase.firestore.Users
import com.client.traveller.data.network.firebase.firestore.model.ChatFirestoreModel
import com.client.traveller.data.network.firebase.firestore.model.Token
import com.client.traveller.data.network.firebase.messaging.CloudMessaging
import com.client.traveller.data.network.firebase.messaging.notifications.Data
import com.client.traveller.data.network.firebase.messaging.notifications.NotificationApiService
import com.client.traveller.data.network.firebase.messaging.notifications.Sender
import com.client.traveller.ui.util.randomUid
import com.client.traveller.ui.util.toFlow
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MessagingRepositoryImpl(
    private val messeageDao: MesseageDao
) : MessagingRepository {

    private val _lastChatsMessageData = mutableMapOf<String, Messeage>()
    private val _lastChatsMessage = MutableLiveData<MutableMap<String, Messeage>>()
    private val lastChatsMessage: LiveData<MutableMap<String, Messeage>>
        get() = _lastChatsMessage
    private val chatsLastMessageObservers = mutableListOf<ListenerRegistration>()
    private var lastChatUidMessagesInitilized: String = ""

    override suspend fun refreshToken() = withContext(Dispatchers.IO) {
        val token = Tokens.getCurrentToken()
        token?.let { Tokens.saveToken(it) }
    }

    /**
     * zapisuje wiadomość do wybranego czatu
     */
    override suspend fun saveMesseage(chat: ChatFirestoreModel, messeage: Messeage) {
        Messeages.saveMesseage(chat.uid!!, messeage)
        this.sendNotifications(chat, messeage)
    }

    /**
     * Wysyłanie powiadomienia o wiadomości do wszystkich osób z chatu
     * z wiadomością w body powiadomienia
     */
    private suspend fun sendNotifications(chat: ChatFirestoreModel, messeage: Messeage) {
        val notifictionService = NotificationApiService()
        val participants = chat.participantsUid?.keys?.toMutableList()
        participants?.remove(messeage.senderIdFirebase)
        val tokens = this.getUsersTokens(participants)
        tokens?.forEach {
            val notificationData = Data(
                messeage.senderIdFirebase!!, R.mipmap.ic_launcher.toString(), messeage.messeage!!,
                "Wiadomość", it.userUid!!, chat.uid!!, chat.tripUid!!
            )
            val sender = Sender(notificationData, it.token!!)
            notifictionService.sendNotification(sender)
        }
    }

    /**
     * zwracaa tokeny użytkowników które są niezbędne do wysyłania wiadomości
     */
    override suspend fun getUsersTokens(userIds: List<String>?): List<Token>? {
        return userIds?.map {
            Tokens.getUserToken(it)
        }
    }

    /**
     * Tworzy nowy czat, zostaje uwzględnieni uczestnicy i numer wycieczki
     * @param participants to idFirebase uczestników tworzonej wycieczki
     */
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
            Chats.addChat(chat).addOnCompleteListener { task ->
                if (task.isSuccessful) continuation.resumeWith(Result.success(true))
                else continuation.resumeWith(Result.success(false))
            }
        }
    }


    /**
     * Wyszukuje czat na podstawie jego uczestników i unikanego numeru wycieczki
     * Powinien być taki tylko jeden
     */
    override suspend fun findChat(participants: ArrayList<String>, tripUid: String) =
        withContext(Dispatchers.IO) {
            suspendCoroutine<ChatFirestoreModel> { continuation ->
                Chats.getChatByParticipantsFiltrSize(participants, tripUid).get()
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
    @ExperimentalCoroutinesApi
    override fun getUsersChats(
        userId: String,
        tripUid: String
    ) = Chats.getUserAllChats(userId, tripUid)
        .toFlow()
        .map {
            it.toObjects(ChatFirestoreModel::class.java).toList()
        }

    /**
     * Szuka czatu podanego jako uid
     */
    override suspend fun findChatByUid(uid: String) = withContext(Dispatchers.IO) {
        suspendCoroutine<ChatFirestoreModel> { continuation ->
            Chats.getChatByUid(uid).get().addOnSuccessListener { snapshot ->
                snapshot?.let {
                    if (it.size() == 1) {
                        val chat = it.first().toObject(ChatFirestoreModel::class.java)
                        continuation.resumeWith(Result.success(chat))
                    }
                }
            }
        }
    }

    private suspend fun saveMessageToLocalDB(messeage: Messeage) {
        this.messeageDao.upsert(messeage)
    }

    private fun isTheSameChat(chatUid: String): Boolean {
        if (this.lastChatUidMessagesInitilized == chatUid) return true
        this.lastChatUidMessagesInitilized = chatUid
        return false
    }


    /**
     * Inicjalizuje i zwraca flow wiadomości z podanego czatu
     */
    @ExperimentalCoroutinesApi
    override fun initChatMessages(chatUid: String): Flow<List<Messeage>> {
//        if (this.isTheSameChat(chatUid)) return this.messeageDao.getAll().asFlow()
        return Messeages.getMesseages(chatUid).orderBy(
            "sendDate",
            Query.Direction.ASCENDING
        )
            .toFlow()
            .onStart {
                this@MessagingRepositoryImpl.messeageDao.deleteAll()
            }
            .onEach {
                it.forEach { query ->
                    this.saveMessageToLocalDB(query.toObject(Messeage::class.java))
                }
            }
            .map {
                it.toObjects(Messeage::class.java).toList()
            }
    }

    /**
     * Inicjalizuje słuchacza na najwcześniejszą wiadomość w czacie
     * Jeżeli ktoś wyśle nową wiadomość do podanego czatu to zotaje wywołany
     */
    override fun initChatLastMesseage(chatUid: String) {
        val observer = Messeages.getMesseages(chatUid)
            .orderBy("sendDate", Query.Direction.DESCENDING).limit(1)
            .addSnapshotListener(EventListener<QuerySnapshot> { querySnapshot, exception ->
                exception?.let { return@EventListener }

                if (querySnapshot != null && !querySnapshot.isEmpty) {
                    val message = querySnapshot.first()?.toObject(Messeage::class.java)
                    message?.let {
                        this._lastChatsMessageData[chatUid] = message
                        Chats.setChatUnSeen(chatUid)
                    }
                    this._lastChatsMessage.value = this._lastChatsMessageData
                }
            })
        this.chatsLastMessageObservers.add(observer)
    }


    /**
     * zwraca liveData ostatniej wiadomości ze wszystkicj zainicjalizowanych czatów
     */
    override fun getChatsLastMessage(): LiveData<MutableMap<String, Messeage>> {
        return this.lastChatsMessage
    }

    /**
     * usuwa słuchacza z ostatnich wiadomości czatów
     */
    override fun initChatsLastMessageRemoveObservers() {
        this.chatsLastMessageObservers.forEach {
            it.remove()
        }
    }
}