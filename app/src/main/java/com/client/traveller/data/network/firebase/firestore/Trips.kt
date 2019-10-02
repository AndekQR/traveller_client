package com.client.traveller.data.network.firebase.firestore

import com.client.traveller.data.db.entities.Trip
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class Trips {

    companion object {
        private const val COLLECTION_NAME = "trips"
    }

    private fun getTripsCollection(): CollectionReference {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME)
    }

    fun getAllTrips(): CollectionReference {
        return this.getTripsCollection()
    }

    fun addNewTrip(trip: Trip): Task<Void> {
        return this.getTripsCollection().document(getDocumentName(trip)).set(trip)
    }

    // nazwa to: nazwa_wycieczki_firebaseUidAuthor
    private fun getDocumentName(trip: Trip): String{
        return "${trip.name}_${trip.author?.idUserFirebase}"
    }
}