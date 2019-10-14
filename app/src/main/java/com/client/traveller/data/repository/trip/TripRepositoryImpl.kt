package com.client.traveller.data.repository.trip

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.client.traveller.data.db.TripDao
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.network.firebase.firestore.Trips
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.suspendCoroutine

class TripRepositoryImpl(
    private val trips: Trips,
    private val tripDao: TripDao
) : TripRepository {

    private val tripList: MutableLiveData<List<Trip>> = MutableLiveData()

    /**
     * Dodanie obserwatora do wycieczek w firestore
     * Przy każdej modyfkikacji tychdanych zostanie zaktualizowany [tripList]
     */
    init {
        this.trips.getAllTrips()
            .addSnapshotListener(EventListener<QuerySnapshot> { querySnapshot, exception ->
                exception?.let {
                    Log.e(javaClass.simpleName, exception.message)
                    return@EventListener
                }

                val trips = mutableListOf<Trip>()
                for (doc in querySnapshot!!) {
                    val trip = doc.toObject(Trip::class.java)
                    trips.add(trip)
                }
                tripList.value = trips
            })
    }


    override suspend fun getAllTrips() = this.tripList

    /**
     * Metoda tworzy nową wycieczkę tzn. zapisuje wycieeczkę do firestore i do lokalnej bazy danych
     * withContext zmmienia kontekst korutyny w jakiej jest wykonywane, sam nie tworzy nowej
     *
     * @param trip wycieczka do zapisania
     */
    override suspend fun newTrip(trip: Trip) = withContext(Dispatchers.IO) {
        suspendCoroutine<Void?> { continuation ->
            trips.addNewTrip(trip).addOnCompleteListener {
                if (!it.isSuccessful) {
                    Log.e(javaClass.simpleName, it.exception?.message)
                    continuation.resumeWith(Result.failure(it.exception!!))
                    return@addOnCompleteListener
                } else {
                    GlobalScope.launch(Dispatchers.IO) {
                        tripDao.upsert(trip)
                    }
                    continuation.resumeWith(Result.success(it.result))
                    return@addOnCompleteListener
                }
            }
        }
    }
}