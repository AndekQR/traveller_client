package com.client.traveller.data.network.firebase.firestore

import com.client.traveller.data.network.firebase.firestore.model.UserLocalization
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Kolekcja przechowuje lokalizacje każdego użytkownika
 * Lokalizacje są podzielone weług uid wycieczek
 *
 * map_coordinates (collection) -> tripUid (document) -> users_localization (collection) -> userUid (document)
 */
object Map {

    private const val COLLECTION_NAME = "map_coordinates"
    private const val TRIPS_USERS_LOCALIZATION = "users_localization"


    /**
     * kolekcja z dokumnetami o nazwie Uid wycieczek
     */
    private fun getMapCoordinatesCollection(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
    }

    /**
     * zwraca referencje do dokumentu który znajduje się w [COLLECTION_NAME]
     * natym etapie nie wiemy czy ten dokument istnieje
     */
    private fun getTripUsersLocalizationDocument(tripUid: String): DocumentReference {
        return this.getMapCoordinatesCollection().document(tripUid)
    }

    /**
     * tworzry nowy dokument w [COLLECTION_NAME]
     */
    private fun createTripDocument(tripUid: String): Task<Void> {
        return this.getMapCoordinatesCollection().document(tripUid).set(hashMapOf("tripUid" to tripUid))
    }

    /**
     * tworzy lub uaktualnia dokument z loalizacją użytkownika
     */
    fun sendNewLocation(userLocalization: UserLocalization, tripUid: String) {
        this.getTripUsersLocalizationDocument(tripUid).get()
            .addOnSuccessListener { snapshot: DocumentSnapshot ->
                if (snapshot.exists()) {
                    this.getTripUsersLocalizationDocument(tripUid)
                        .collection(TRIPS_USERS_LOCALIZATION)
                        .document(userLocalization.userUidFirebase!!).set(userLocalization)
                } else {
                    this.createTripDocument(tripUid).addOnSuccessListener {
                        this.getTripUsersLocalizationDocument(tripUid)
                            .collection(TRIPS_USERS_LOCALIZATION)
                            .document(userLocalization.userUidFirebase!!).set(userLocalization)
                    }
                }
            }
    }

    fun getUserLocalizationCollection(tripUid: String): CollectionReference {
        return this.getTripUsersLocalizationDocument(tripUid).collection(TRIPS_USERS_LOCALIZATION)
    }


}