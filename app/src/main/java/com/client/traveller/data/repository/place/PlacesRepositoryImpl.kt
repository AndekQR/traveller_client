package com.client.traveller.data.repository.place

import android.os.Build
import android.util.Log
import androidx.core.text.HtmlCompat
import com.client.traveller.data.network.api.places.API_KEY
import com.client.traveller.data.network.api.places.PlacesApiService
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
import com.client.traveller.data.network.api.places.response.placeDetailResponse.PlaceDetailResponse
import com.client.traveller.data.network.api.wikipedia.WikipediaApiService
import com.client.traveller.data.network.api.wikipedia.model.Section
import com.client.traveller.data.network.api.wikipedia.response.wikipediaPageSummaryResponse.WikipediaPageSummaryResponse
import com.client.traveller.data.network.api.wikipedia.response.wikipediaPrefixSearchResponse.WikipediaPrefixSearchResponse
import com.client.traveller.data.provider.PreferenceProvider
import com.client.traveller.ui.util.contains
import java.util.*

class PlacesRepositoryImpl(
    private val placesApiClient: PlacesApiService,
    private val wikipediaApiService: WikipediaApiService,
    private val preferencesProvider: PreferenceProvider
) : PlacesRepository {

    private val searchedTypes = listOf(
        "restaurant",
        "amusement_park",
        "aquarium",
        "art_gallery",
        "bar",
        "bowling_alley",
        "cafe",
        "casino",
        "church",
        "library",
        "museum",
        "park",
        "tourist_attraction",
        "zoo"
    )

    override suspend fun getNearbyPlaces(latlng: String, radius: Int?): Set<Result> {

        val myRadius = radius ?: preferencesProvider.getNearbyPlacesSearchDistance()
        val response = if (myRadius != null) this.placesApiClient.findNearbyPlaces(latlng = latlng, radius = myRadius)
            else this.placesApiClient.findNearbyPlaces(latlng = latlng)

        val asd = response
        return response.results.filter { it.types.contains(this.searchedTypes) }.toSet()
    }

    override fun getPhotoUrl(reference: String, width: Int): String {
        return "${PlacesApiService.BASE_URL}photo?maxwidth=${width}&photoreference=${reference}&key=$API_KEY"
    }

    override fun getSearchedTypes(): List<String> {
        return this.searchedTypes
    }

    override suspend fun getPlaceDetail(placeId: String): PlaceDetailResponse {
        return placesApiClient.getPlaceDetail(placeId)
    }

    /**
     * Zwraca tablicę obiektów które mają numer strony wikipedii i tytuł strony
     */
    override suspend fun getPrefixes(query: String): WikipediaPrefixSearchResponse {
        return this.wikipediaApiService.searchPrefixes(query = query)
    }

    /**
     * otrzymujemy opis zagadnienia ze strony z podanym tytułem
     */
    override suspend fun getPageSummary(pageTitle: String): WikipediaPageSummaryResponse {
        return this.wikipediaApiService.getPageSummary(pageTitle)
    }

    /**
     * zwraca obiektu typu section ktore zawierają odkodowany nagłówek i treść sekcji z wikipedia
     */
    override suspend fun getPageSections(pageTitle: String): List<Section> {
        val reponse = this.wikipediaApiService.getSectionsHtml(pageTitle)
        val sectionsHTML = reponse.remaining.sections
        val sections = mutableListOf<Section>()
        sectionsHTML.forEachIndexed { _, sectionX ->
            val title = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                android.text.Html.fromHtml(sectionX.anchor, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    .toString().replace("_", " ")
            } else {
                android.text.Html.fromHtml(sectionX.anchor).toString().replace("_", " ")
            }
            val text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                android.text.Html.fromHtml(sectionX.text, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    .toString()
            } else {
                android.text.Html.fromHtml(sectionX.anchor).toString()
            }
            if (text.length > 20 && title.isNotEmpty() && title.toLowerCase(Locale.ROOT) != "bibliografia" && title.toLowerCase(
                    Locale.ROOT
                ) != "przypisy" && title.toLowerCase(Locale.ROOT) != "zobacz też"
            ) {
                val section = Section(title, text)
                sections.add(section)
            }
        }
        return sections
    }

}