package com.client.traveller

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.client.traveller.data.network.map.LocationBroadcastReceiver
import com.client.traveller.data.provider.PreferenceProvider
import com.client.traveller.data.services.MyLocationService
import com.google.android.material.snackbar.Snackbar
import org.kodein.di.KodeinAware

abstract class BaseActivity : AppCompatActivity(), KodeinAware {

    companion object {
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }

    var locationService: MyLocationService? = null
    /**
     * do śledzenia stanu serwisu
     * true = serwis działa, nie jest wymagane powiadomienie bo apikacja też działa i jest na pierwszym planie
     * false = serwis powinien działać, w serwisie tworzone jest powiadomienie aby serwis działał w tle
     */
    var mBound = false
    private var receiver: BroadcastReceiver? = null

    private val locationServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            this@BaseActivity.locationService = null
            mBound = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MyLocationService.LocalBinder
            this@BaseActivity.locationService = binder.service
            mBound = true
            if (!checkPermissions()) {
                requestPermissions()
            } else {
                locationService?.startLocationUpdates()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // jeżeli nie ma uprawnień do lokalizacji, poproś o uprawnienia
        if (PreferenceProvider(this).getSendLocation()) {
            if (!checkPermissions()) {
                requestPermissions()
            }
        }
        this.receiver = LocationBroadcastReceiver()
    }

    override fun onStart() {
        super.onStart()

        if (!checkPermissions()) {
            requestPermissions()
        } else {
            locationService?.startLocationUpdates()
        }

        /**
         * Powiadomienie dla serwisu o tym że aplikacja jest na pierwszym planie
         * więc serwis może usunąc powiadomienie
         */
        this.bindService(
            Intent(this, MyLocationService::class.java), locationServiceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        super.onStop()
        /**
         * Powiadomienie dla serwisu o tym że aplikacja nie jest na pierwszym planie
         * serwis musi utworzyć powaidomienie przez które nie zostanie zamknięty
         */
        this.unbindService(locationServiceConnection)
        mBound = false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.locationService?.startLocationUpdates()
            } else { // brak uprawnień
                Snackbar.make(
                    window.decorView.rootView,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        this.openSettings()
                    }
                    .show()
            }
        }
    }

    private fun openSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts(
            "package",
            BuildConfig.APPLICATION_ID, null
        )
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    /**
     * wywoływana po ponownym stworzeniu aktywności
     */
    override fun onResume() {
        super.onResume()

        /**
         * rejestracja klasy LocationBroadcastReceiver jako klasy która odbiera dane z MyLocationService
         */
        LocalBroadcastManager.getInstance(this).registerReceiver(
            receiver!!,
            IntentFilter(MyLocationService.ACTION_BROADCAST)
        )
    }

    /**
     * wywoływana gdy aktywność przejdzie na drugi plan ( zakryje ją inna aktywność )
     */
    override fun onPause() {
        super.onPause()
        /**
         * dane z serwisu nie są już potrzebne bo aplikacja przeszła na inny plan
         */
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver!!)

    }

    private fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestPermissions() {
        val shouldShowRequestPermissionRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        if (shouldShowRequestPermissionRationale) {
            Snackbar.make(
                window.decorView.rootView,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    ActivityCompat.requestPermissions(
                        this@BaseActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            ActivityCompat.requestPermissions(
                this@BaseActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }
}