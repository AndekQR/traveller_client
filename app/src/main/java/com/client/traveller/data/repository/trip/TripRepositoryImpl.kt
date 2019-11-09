package com.client.traveller.data.repository.trip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.client.traveller.data.db.TripDao
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.firestore.Trips
import com.client.traveller.data.network.firebase.firestore.Users
import com.client.traveller.ui.util.Coroutines.io
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.suspendCoroutine

class TripRepositoryImpl(
    private val trips: Trips,
    private val tripDao: TripDao
) : TripRepository {

    private val tripList: MutableLiveData<List<Trip>> = MutableLiveData()
    private val currentTrip: LiveData<Trip> = this.tripDao.getCurrentTrip()


    init {
        this.initAllTrips()
        this.initCurrentTripUpdates()
    }

    /**
     * Dodanie obserwatora do wycieczek w firestore
     * Przy każdej modyfkikacji tychdanych zostanie zaktualizowany [tripList]
     */
    private fun initAllTrips() {
        this.trips.getAllTrips()
            .addSnapshotListener(EventListener<QuerySnapshot> { querySnapshot, exception ->
                exception?.let {
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

    /**
     * aktualizcja lokalnej currentTrip gdy inny użytkownik wyśle do firestore zmiany do tej wycieczki
     */
    private fun initCurrentTripUpdates() = io {
        val currentTrip = this.tripDao.getCurrentTripNonLive()
        trips.getTrip(currentTrip.uid!!).addSnapshotListener(EventListener<QuerySnapshot> {querySnapshot, excetion ->
            excetion?.let { return@EventListener }

            val updatedCurrentTrip = querySnapshot?.first()?.toObject(Trip::class.java)
            updatedCurrentTrip?.let {
                io{tripDao.upsert(it)}
            }
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
                    continuation.resumeWith(Result.failure(it.exception!!))
                    return@addOnCompleteListener
                } else {
                    this@TripRepositoryImpl.saveTripToLocalDB(trip)
                    continuation.resumeWith(Result.success(it.result))
                    return@addOnCompleteListener
                }
            }
        }
    }

    override fun saveTripToLocalDB(trip: Trip)= io {
        this.tripDao.upsert(trip)
        this.initCurrentTripUpdates()
    }

    override fun getCurrentTrip() = this.currentTrip
    override fun isTripParticipant(trip: Trip, user: User): Boolean {
        trip.persons?.forEach {
            if (it == user.email)
                return true
        }
        return false
    }

    override fun updateTripPersons(trip: Trip, emails: List<String>) {
        this.trips.updateTripPersons(trip, ArrayList(emails))
    }

}