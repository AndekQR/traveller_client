package com.client.traveller.data.network.map

import com.client.traveller.data.db.entities.User
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class UserLocationClusterItem(
    val latlng: LatLng,
    val user: User
): ClusterItem {

    override fun getSnippet(): String? {
        return null
    }
    override fun getTitle(): String {
        return "${user.displayName}"
    }

    override fun getPosition(): LatLng {
        return latlng
    }

}