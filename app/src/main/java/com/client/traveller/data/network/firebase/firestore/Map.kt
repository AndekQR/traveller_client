package com.client.traveller.data.network.firebase.firestore

import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.firestore.model.UserLocalization
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Kolekcja przechowuje lokalizacje każdego użytkownika
 * Lokalizacje są podzielone weług uid wycieczek
 */
object Map {

    private const val COLLECTION_NAME = "map"
    private const val TRIPS_USERS_LOCALIZATION = "trips_users_localization"


    private fun getMapCollection(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
    }

    private fun getTripUsersLocalizationCollection(tripUid: String): CollectionReference {
        return this.getMapCollection().document(tripUid).collection(TRIPS_USERS_LOCALIZATION)
    }

    fun sendNewLocation(userLocalization: UserLocalization, tripUid: String) {
        this.getTripUsersLocalizationCollection(tripUid)
            .document(userLocalization.userUidFirebase!!).set(userLocalization)
    }

    fun getTripUsersLocation(tripUid: String): CollectionReference {
        return this.getTripUsersLocalizationCollection(tripUid)
    }
}