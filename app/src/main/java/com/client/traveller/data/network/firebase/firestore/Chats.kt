package com.client.traveller.data.network.firebase.firestore

import com.client.traveller.data.network.firebase.firestore.model.ChatFirestoreModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Chats {

    companion object {
        const val COLLECTION_NAME = "chats"
    }

    private fun getCollectionReference(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
    }

    /**
     * Pierwsze zapytanie zwraca wszystkie chaty które mają odpowiednią liczbę uczestników
     * nestępne zapytania w pętli szukają w chatach pozostałych uczestników
     *
     * @param participants lista uidFirebase uczestników szukanego chatu, muszą być wszyscy
     */
    fun getChatByParticipants(participants: ArrayList<String>): Query {
        var result = this.getCollectionReference().whereEqualTo("participantsNumber", participants.size)
        for (i in 0 until participants.size) {
            result = result.whereEqualTo("participantsUid.${participants[i]}", true)
        }
        return result
    }

    fun addChat(chat: ChatFirestoreModel): Task<Void> {
        return this.getCollectionReference().document(chat.uid!!).set(chat)
    }
}