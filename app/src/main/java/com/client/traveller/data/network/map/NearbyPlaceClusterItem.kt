package com.client.traveller.data.network.map

import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class NearbyPlaceClusterItem(val place: Result): ClusterItem {

    override fun getSnippet(): String? {
        return null
    }

    override fun getTitle(): String {
        return "${place.name}\n" +
                place.vicinity
    }

    override fun getPosition(): LatLng {
        return LatLng(place.geometry.location.lat, place.geometry.location.lng)
    }

}