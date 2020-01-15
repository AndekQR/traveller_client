package com.client.traveller

import android.text.Spanned
import android.util.Log
import com.client.traveller.data.network.api.places.PlacesApiService
import com.client.traveller.data.network.api.wikipedia.WikipediaApiService
import com.client.traveller.data.network.api.wikipedia.model.Section
import com.client.traveller.data.network.api.wikipedia.response.wikipediaSectionsResponse.SectionX
import com.client.traveller.data.provider.PreferenceProvider
import com.client.traveller.data.repository.place.PlacesRepositoryImpl
import io.kotlintest.Spec
import io.kotlintest.matchers.collections.shouldHaveSingleElement
import io.kotlintest.matchers.string.shouldBeEqualIgnoringCase
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking

class PlacesRepositoryTests : StringSpec() {

    private lateinit var placesRepositoryImpl: PlacesRepositoryImpl
    private val placesApiService: PlacesApiService = mockk()
    private val wikipediaApiService: WikipediaApiService = mockk()
    private val preferenceProvider: PreferenceProvider = mockk()

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)

        this.placesRepositoryImpl =
            PlacesRepositoryImpl(placesApiService, wikipediaApiService, preferenceProvider)
    }

    init {

        "getPhotoUrlTest" {
            val reference = "REFERENCE"
            val width = 100
            val expected =
                "https://maps.googleapis.com/maps/api/place/photo?maxwidth=${width}&photoreference=${reference}&key=AIzaSyAd9mVHXtr7oJkIwt605x3Wu5A65srtq6Q"
            val returned = placesRepositoryImpl.getPhotoUrl(reference, width)
            expected shouldBeEqualIgnoringCase returned
        }

        "getPageSectionsTest" {
            val pageTitle = "dom"
            val title = "Historia"
            val htmlText =
                "\n<p>Najstarszą formą domów były jaskinie (<a href=\"/wiki/Paleolit\" title=\"Paleolit\">paleolit</a>), później szałasy i ziemianki (<a href=\"/wiki/Mezolit\" title=\"Mezolit\">mezolit</a>)"
            val text = "Najstarszą formą domów były jaskinie, później szałasy i ziemianki"
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

            every { android.text.Html.fromHtml(any()).toString() } returns "Historia"

            val returned = placesRepositoryImpl.getPageSections(pageTitle)
            returned shouldHaveSingleElement sections.first()
        }
    }
}
