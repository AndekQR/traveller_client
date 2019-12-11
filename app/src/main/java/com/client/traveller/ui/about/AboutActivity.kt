package com.client.traveller.ui.about

import android.location.Location
import android.os.Bundle
import com.client.traveller.ui.BaseActivity
import com.client.traveller.R

class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
    }

    override fun onNewLocation(location: Location) {

    }
}
