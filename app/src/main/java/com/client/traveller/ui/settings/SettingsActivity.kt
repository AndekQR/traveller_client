package com.client.traveller.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.client.traveller.R
import com.client.traveller.data.provider.LocationProvider
import kotlinx.android.synthetic.main.settings_activity.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class SettingsActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: SettingsViewModelFactory by instance()
    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, factory).get(SettingsViewModel::class.java)

        setContentView(R.layout.settings_activity)
        setSupportActionBar(toolbar)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment(viewModel.locationProvider))
            .commit()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

    }

    class SettingsFragment(private val locationProvider: LocationProvider) :
        PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            (activity as? AppCompatActivity)?.supportActionBar?.title =
                getString(R.string.title_activity_settings)
            (activity as? AppCompatActivity)?.supportActionBar?.subtitle = null

            val sendingLocation = findPreference<SwitchPreference>("SEND_LOCATION")
            sendingLocation?.onPreferenceChangeListener = locationProvider
        }
    }
}