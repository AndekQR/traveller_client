package com.client.traveller.data.network.firebase.firestore

import com.client.traveller.data.network.firebase.firestore.model.ChatFirestoreModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object Chats {


    const val COLLECTION_NAME = "chats"


    private fun getCollectionReference(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
    }

    /**
     * Pierwsze zapytanie zwraca wszystkie chaty które mają odpowiednią liczbę uczestników
     * nestępne zapytania w pętli szukają w chatach pozostałych uczestników
     *
     * @param participants lista uidFirebase uczestników szukanego chatu, muszą być wszyscy
     */
    fun getChatByParticipantsFiltrSize(participants: ArrayList<String>, tripUid: String): Query {
        var result =
            this.getCollectionReference().whereEqualTo("participantsNumber", participants.size)
                .whereEqualTo("tripUid", tripUid)
        for (i in 0 until participants.size) {
            result = result.whereEqualTo("participantsUid.${participants[i]}", true)
        }
        return result
    }

    /**
     * Zwraca wszystkie czaty podanego usera w podanej wycieczce
     *
     * @param userId idFirebase użytkownika
     * @param tripUid uid wycieczki
     */
    fun getUserAllChats(userId: String, tripUid: String): Query {
        return this.getCollectionReference().whereEqualTo("tripUid", tripUid)
            .whereEqualTo("participantsUid.$userId", true)
    }

    fun addChat(chat: ChatFirestoreModel): Task<Void> {
        return this.getCollectionReference().document(chat.uid!!).set(chat)
    }

    fun getChatByUid(uid: String): Query {
        return this.getCollectionReference().whereEqualTo("uid", uid)
    }

    fun setChatUnSeen(chaUid: String) {
        this.getCollectionReference().document(chaUid).update("isSeen", false)
    }
}