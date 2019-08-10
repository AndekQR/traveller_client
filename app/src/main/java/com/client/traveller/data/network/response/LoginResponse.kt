package com.client.traveller.data.network.response

import com.client.traveller.data.db.entities.User

data class LoginResponse(
    val token: String?,
    val user: User?
)