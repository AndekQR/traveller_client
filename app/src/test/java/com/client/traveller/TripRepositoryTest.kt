package com.client.traveller

import com.client.traveller.data.db.TripDao
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.repository.trip.TripRepositoryImpl
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TripRepositoryTest {

    private val tripDao: TripDao = mockk()
    private lateinit var tripRepositoryImpl: TripRepositoryImpl

    @BeforeAll
    fun init() {
        this.tripRepositoryImpl = TripRepositoryImpl(this.tripDao)
    }

    @Test
    fun isTripParticipantTest() {
        val user = User("TEST", "TEST", "TEST@TEST.TEST", true, null)
        val trip = Trip(
            "TEST", arrayListOf(user.email!!), "2020-12-20T12:52",
            "2019-12-25T12:52", "Staszów", "Połaniec", arrayListOf(),
            user, "TEST"
        )
        Assertions.assertTrue(tripRepositoryImpl.isTripParticipant(trip, user))
    }
}