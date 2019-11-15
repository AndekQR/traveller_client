package com.client.traveller.data.network.firebase.messaging.notifications


data class Data(
    var sentFrom: String,
    var icon: String,
    var body: String,
    var title: String,
    var sentTo: String,
    var chatUid: String,
    var tripUid: String
    )