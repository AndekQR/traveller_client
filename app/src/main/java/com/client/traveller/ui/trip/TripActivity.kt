package com.client.traveller.ui.trip

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.R

import kotlinx.android.synthetic.main.activity_trip.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class TripActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: TripViewModelFactory by instance()
    private lateinit var viewModel: TripViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip)

        viewModel = ViewModelProvider(this, factory).get(TripViewModel::class.java)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount

        if (count == 0) {
            super.onBackPressed()
        } else {
            supportFragmentManager.popBackStackImmediate()
        }
    }

}
