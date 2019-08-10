package com.client.traveller.ui.auth

import com.client.traveller.data.db.entities.User

interface AuthListener {

    fun onStarted()
    fun onSuccess(user: User)
    fun onFailure(message: String)

}