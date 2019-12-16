package com.client.traveller.ui.about

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import com.client.traveller.ui.BaseActivity
import com.client.traveller.R
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.activity_about.toolbar
import kotlinx.android.synthetic.main.activity_trip.*

class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        contact_author_button.setOnClickListener(onContactButtonClick)

    }

    private val onContactButtonClick = View.OnClickListener {
        Intent(Intent.ACTION_SEND).also {

            val bodyText = "Device model: ${Build.MANUFACTURER} \n" +
                    "Android version: ${Build.VERSION.BASE_OS}\n" +
                    "Your information: "
            it.type = "message/rfc822"
            it.putExtra(Intent.EXTRA_SUBJECT, "Traveller application")
            it.putExtra(Intent.EXTRA_TEXT, bodyText)
            it.putExtra(Intent.EXTRA_EMAIL, arrayOf("com.app.traveller@gmail.com"))
            startActivity(Intent.createChooser(it, "Shearing Option"))
        }
    }

    override fun onNewLocation(location: Location) {}
}
