package com.client.traveller.data.repository.trip

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.client.traveller.data.db.TripDao
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.network.firebase.firestore.Trips
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TripRepositoryImpl(
    private val trips: Trips,
    private val tripDao: TripDao
) : TripRepository {

    private val tripList: MutableLiveData<List<Trip>> = MutableLiveData()

    override suspend fun getAllTrips(): MutableLiveData<List<Trip>> {

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

        return this.tripList
    }

    override suspend fun newTrip(trip: Trip)= withContext(Dispatchers.IO) {
        Log.e(javaClass.simpleName, "3")
        suspendCoroutine<Void?> { continuation ->
            trips.addNewTrip(trip).addOnCompleteListener {
                if (!it.isSuccessful) {
                    Log.e(javaClass.simpleName, it.exception?.message)
                    continuation.resumeWith(Result.failure(it.exception!!))
                    return@addOnCompleteListener
                } else {
                    Log.e(javaClass.simpleName, "4")
                    GlobalScope.launch(Dispatchers.IO){
                        tripDao.upsert(trip)
                    }
                    continuation.resumeWith(Result.success(it.result))
                    return@addOnCompleteListener
                }
            }
        }
    }
}