package com.client.traveller.unit

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.client.traveller.data.db.AppDatabase
import com.client.traveller.data.db.TripDao
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.repository.trip.TripRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TripRepositoryTests {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var tripDao: TripDao
    private lateinit var tripRepositoryImpl: TripRepositoryImpl


    @BeforeAll
    fun createDB() {
        this.context = ApplicationProvider.getApplicationContext()
        this.db = Room.inMemoryDatabaseBuilder(this.context, AppDatabase::class.java).build()
        this.tripDao = this.db.tripDao()
        this.tripRepositoryImpl = TripRepositoryImpl(this.tripDao)
    }

    @AfterAll
    fun closeDB() {
        this.db.close()
    }

    @Test
    fun shoudlSaveToDB() {
        val user = User("TEST", "TEST", "TEST@TEST.TEST", true, null)
        val trip = Trip(
            "TEST", arrayListOf(), "2020-12-20T12:52",
            "2019-12-25T12:52", "Staszów", "Połaniec", arrayListOf(),
            user, "TEST"
        )
        var savedTrip: Trip? = null
        runBlocking {
           tripDao.upsert(trip)
            val savedTrip = this@TripRepositoryTests.tripDao.getCurrentTripNonLive()
            Assertions.assertEquals(trip.uid, savedTrip?.uid)
        }

    }
}