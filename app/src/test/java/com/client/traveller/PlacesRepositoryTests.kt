package com.client.traveller

import com.client.traveller.data.network.api.places.PlacesApiService
import com.client.traveller.data.network.api.wikipedia.WikipediaApiService
import com.client.traveller.data.provider.PreferenceProvider
import com.client.traveller.data.repository.place.PlacesRepositoryImpl
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlacesRepositoryTests {

    private lateinit var placesRepositoryImpl: PlacesRepositoryImpl
    private val placesApiService: PlacesApiService = mockk()
    private val wikipediaApiService: WikipediaApiService = mockk()
    private val preferenceProvider: PreferenceProvider = mockk()

    @BeforeAll
    fun init() {
        this.placesRepositoryImpl =
            PlacesRepositoryImpl(placesApiService, wikipediaApiService, preferenceProvider)
    }

    @Test
    fun getPhotoUrlTest() {
        val reference = "REFERENCE"
        val width = 100
        val expected =
            "https://maps.googleapis.com/maps/api/place/photo?maxwidth=${width}&photoreference=${reference}&key=AIzaSyAd9mVHXtr7oJkIwt605x3Wu5A65srtq6Q"
        val returned = placesRepositoryImpl.getPhotoUrl(reference, width)
        Assertions.assertEquals(expected, returned)
    }
}
