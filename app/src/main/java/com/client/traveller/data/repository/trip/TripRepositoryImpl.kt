package com.client.traveller.data.repository.trip

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.client.traveller.data.db.TripDao
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.firestore.Trips
import com.client.traveller.data.provider.PreferenceProvider
import com.client.traveller.ui.util.Coroutines.io
import com.client.traveller.ui.util.toFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlin.coroutines.suspendCoroutine

@ExperimentalCoroutinesApi
class TripRepositoryImpl(
    private val tripDao: TripDao
) : TripRepository {

    private lateinit var tripList: LiveData<List<Trip>>

    init {
        this.initAllTrips()
        GlobalScope.launch(Dispatchers.IO) { this@TripRepositoryImpl.initCurrentTripUpdates() }
    }

    /**
     * Dodanie obserwatora do wycieczek w firestore
     * Przy każdej modyfkikacji tychdanych zostanie zaktualizowany [tripList]
     */
    @ExperimentalCoroutinesApi
    private fun initAllTrips() {
        this.tripList =
            Trips.getAllTrips().toFlow().map { it.toObjects(Trip::class.java) }.asLiveData()
    }

    /**
     * aktualizcje lokalnej currentTrip gdy inny użytkownik wyśle do firestore zmiany do tej wycieczki
     * wybieramy pierwszą bo w liście powinna być tylko jedna wycieczka o takim uid
     */
    // TODO mogą być problemy -> trzeab sprawdzić
    @ExperimentalCoroutinesApi
    override suspend fun initCurrentTripUpdates() {
        val currentTripUid = this.tripDao.getCurrentTripNonLive()?.uid
        currentTripUid?.let {
            Trips.getTrip(it).toFlow().map { it.toObjects(Trip::class.java).toList() }
                .collect { list ->
                    if (list.isNotEmpty()) this.tripDao.upsert(list.first())
                }
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun getAllTrips(): LiveData<List<Trip>> {
        return if (!::tripList.isInitialized) {
            this.initAllTrips()
            this.tripList
        } else {
            this.tripList
        }
    }

    /**
     * Metoda tworzy nową wycieczkę tzn. zapisuje wycieeczkę do firestore i do lokalnej bazy danych
     * withContext zmmienia kontekst korutyny w jakiej jest wykonywane, sam nie tworzy nowej
     *
     * @param trip wycieczka do zapisania
     */
    override suspend fun newTrip(trip: Trip, context: Context) = withContext(Dispatchers.IO) {
        suspendCoroutine<Void?> { continuation ->
            Trips.addNewTrip(trip).addOnCompleteListener {
                if (!it.isSuccessful) {
                    continuation.resumeWith(Result.failure(it.exception!!))
                    return@addOnCompleteListener
                } else {
                    io { this@TripRepositoryImpl.saveTripToLocalDB(trip, context) }
                    continuation.resumeWith(Result.success(it.result))
                    return@addOnCompleteListener
                }
            }
        }
    }

    /**
     * z uid aktualnej wycieczki korzysta MyLocationService
     * pobiera to uid żeby zapisać aktualną lokalizacje dofirestore
     * wfirestore uid aktualnej wycieczki jest nazwą kolekcji
     */
    @ExperimentalCoroutinesApi
    override suspend fun saveTripToLocalDB(trip: Trip, context: Context) {
        PreferenceProvider(context).putCurrentTravelUid(trip.uid!!)
        this.tripDao.upsert(trip)
        this.initCurrentTripUpdates()
    }

    override fun getCurrentTrip() = this.tripDao.getCurrentTrip()
    override fun isTripParticipant(trip: Trip, user: User): Boolean {
        trip.persons?.forEach {
            if (it == user.email)
                return true
        }
        return false
    }

    override fun updateTripPersons(trip: Trip, emails: List<String>) {
        Trips.updateTripPersons(trip, ArrayList(emails))
    }

    override suspend fun getTripByUid(tripUid: String) = withContext(Dispatchers.IO) {
        suspendCoroutine<Trip> { continuation ->
            Trips.getTrip(tripUid).get().addOnSuccessListener { querySnapshot ->
                val trip = querySnapshot.first().toObject(Trip::class.java)
                continuation.resumeWith(Result.success(trip))
            }
        }
    }

    override fun updateWaypoints(waypoints: List<String>, tripToUpdate: Trip) {
        Trips.updateWaypoints(ArrayList(waypoints), tripToUpdate)
    }

}