package com.client.traveller.unit

import com.client.traveller.data.network.api.places.PlacesApiService
import com.client.traveller.data.network.api.wikipedia.WikipediaApiService
import com.client.traveller.data.network.api.wikipedia.model.Section
import com.client.traveller.data.network.api.wikipedia.response.wikipediaSectionsResponse.SectionX
import com.client.traveller.data.provider.PreferenceProvider
import com.client.traveller.data.repository.place.PlacesRepositoryImpl
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
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
    fun getPageSectionsTest() {
        val pageTitle = "dom"
        val title = "Historia"
        val htmlText =
            "\n<p>Najstarszą formą domów były jaskinie (<a href=\"/wiki/Paleolit\" title=\"Paleolit\">paleolit</a>), później szałasy i ziemianki (<a href=\"/wiki/Mezolit\" title=\"Mezolit\">mezolit</a>)"
        val text = "Najstarszą formą domów były jaskinie (paleolit), później szałasy i ziemianki (mezolit)"
        val sections = mutableListOf<Section>()
        sections.add(Section(title, text))
        val sectionsX = mutableListOf<SectionX>()
        sectionsX.add(SectionX("Historia", 1, false, "", htmlText, 1))

        every {
            runBlocking {
                wikipediaApiService.getSectionsHtml(any())
            } } returns mockk wikiResp@{
            every { this@wikiResp.remaining } returns mockk remaining@{
                every { this@remaining.sections } returns sectionsX.toList()
            }
        }

        runBlocking {
            val returned = placesRepositoryImpl.getPageSections(pageTitle)
            Assertions.assertEquals(returned.first().text.trim(), sections.first().text.trim())
        }
    }
}