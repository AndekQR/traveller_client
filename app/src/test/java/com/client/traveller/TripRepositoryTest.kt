package com.client.traveller

import com.client.traveller.data.db.TripDao
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.firestore.Trips
import com.client.traveller.data.repository.trip.TripRepositoryImpl
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import io.kotlintest.Spec
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot

class TripRepositoryTest: StringSpec() {

    private val tripDao: TripDao = mockk()
    private lateinit var tripRepositoryImpl: TripRepositoryImpl

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        mockkObject(Trips)
        this.tripRepositoryImpl = TripRepositoryImpl(this.tripDao)
    }

    init {
        "shoudl check if the person is trip particpiant" {
            val user = User("TEST", "TEST", "TEST@TEST.TEST", true, null)
            val trip = Trip(
                "TEST", arrayListOf(user.email!!), "2020-12-20T12:52",
                "2019-12-25T12:52", "Staszów", "Połaniec", arrayListOf(),
                user, "TEST"
            )
            this@TripRepositoryTest.tripRepositoryImpl.isTripParticipant(trip, user) shouldBe true
         }

    }

}