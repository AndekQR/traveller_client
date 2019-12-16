package com.client.traveller.data.network.firebase.firestore.model

data class UserLocalization(
    var latlng: MyLatLng? = null,
    var userUidFirebase: String? = null
)