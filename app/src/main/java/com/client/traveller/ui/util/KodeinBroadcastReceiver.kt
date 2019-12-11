package com.client.traveller.ui.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein

abstract class KodeinBroadcastReceiver : BroadcastReceiver(), KodeinAware {
    private lateinit var context: Context
    override val kodein: Kodein by kodein { context }

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        onBroadcastReceived(context, intent)
    }

    abstract fun onBroadcastReceived(context: Context, intent: Intent)
}