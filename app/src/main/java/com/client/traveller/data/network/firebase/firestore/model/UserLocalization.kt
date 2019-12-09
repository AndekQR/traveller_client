package com.client.traveller.data.network.firebase.firestore.model

import com.client.traveller.data.db.entities.User
import com.google.android.gms.maps.model.LatLng

data class UserLocalization(
    var latlng: LatLng? = null,
    var user: User? = null
)