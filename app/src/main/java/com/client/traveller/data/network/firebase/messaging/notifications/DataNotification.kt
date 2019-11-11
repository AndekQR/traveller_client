package com.client.traveller.data.network.firebase.messaging.notifications

import com.client.traveller.data.db.entities.User

data class DataNotification(
    var user: User,
    var body: String,
    var title: String,
    var sendet: String
    )