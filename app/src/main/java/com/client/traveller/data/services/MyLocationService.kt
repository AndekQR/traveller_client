package com.client.traveller.data.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.client.traveller.R
import com.client.traveller.data.provider.PreferenceProvider
import com.client.traveller.ui.home.HomeActivity
import com.google.android.gms.location.*


class MyLocationService : Service() {

    companion object {
        private const val PACKAGE_NAME = "com.client.traveller.data.services.MyLocationService"
        private val TAG = MyLocationService::class.java.simpleName
        private const val CHANNEL_ID = "channel_01"
        const val ACTION_BROADCAST = "$PACKAGE_NAME.broadcast"
        const val EXTRA_LOCATION = "$PACKAGE_NAME.location"
        private const val EXTRA_STARTED_FROM_NOTIFICATION = "$PACKAGE_NAME.started_from_notification"

        private const val UPDATE_INTERVAL_MS: Long = 10000
        private const val FASTEST_UPDATE_INTERVAL_MS = UPDATE_INTERVAL_MS / 2

        private const val NOTIFICATION_ID = 12345678
    }

    private val binder: IBinder = LocalBinder()
    // do sprawdzania czy nastąpiła zmina ustawień system czy aplikacja została zamknięta
    private var changingConfiguration = false
    private var notificationManager: NotificationManager? = null
    private var locationRequest: LocationRequest? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var serviceHandler: Handler? = null
    private var currentLocation: Location? = null

    override fun onCreate() {
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        this.locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                this@MyLocationService.onNewLocation(locationResult.lastLocation)
            }
        }
        this.initLocationrequest()
        this.getLastLocation()

        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        serviceHandler = Handler(handlerThread.looper)
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            this.notificationManager?.createNotificationChannel(mChannel)
        }
    }

    /**
     * startuje gdy wywoła się startService()
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // jeżeli true to user kliknął w powiadomienie w przycisk wyłczający lokalizacje
        val startedFromNotification = intent.getBooleanExtra(
            EXTRA_STARTED_FROM_NOTIFICATION,
            false
        )
        if (startedFromNotification) {
            stopLocationUpdates()
            stopSelf()
        }
        return START_NOT_STICKY
    }

    /**
     * wywoływana przez system w momencie zmiany konfiguracji systemu
     * np. zmiana orientacji ekranu
     * gdy orientacja się zmieni aktywności są restartowane
     * lecz np. serwisy nie. Dlatego trzeba to obsłużyć
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        changingConfiguration = true
    }

    /**
     * wywoływana gdy aktywnść zbinduje ten serwis
     */
    override fun onBind(intent: Intent): IBinder? {
        this.stopForeground(true)
        changingConfiguration = false
        return binder
    }

    /**
     * wywoływana gdy aktywność wróci na pierwszy plan i zbinduje ten serwis
     */
    override fun onRebind(intent: Intent) {
        this.stopForeground(true)
        changingConfiguration = false
        super.onRebind(intent)
    }

    /**
     * wywoływana gdy aktywność unbinduje ten serwis lub gdy system zmieni konfigurację
     * jeżeli zmiana konfiguracji to nie zmieniamy stanu serwisu
     */
    override fun onUnbind(intent: Intent): Boolean {
        val requestingUpdates = PreferenceProvider(this).getSendLocation()
        if (!changingConfiguration && requestingUpdates) {
            this.startForeground(NOTIFICATION_ID, this.getNotification())
        }
        return true
    }

    override fun onDestroy() {
        serviceHandler!!.removeCallbacksAndMessages(null)
    }

    fun startLocationUpdates() {
        Utils.setRequestingLocationUpdates(this, true)
        this.startService(Intent(applicationContext, MyLocationService::class.java))
        this.fusedLocationClient!!.requestLocationUpdates(
            locationRequest,
            locationCallback, Looper.myLooper()
        )
    }

    fun stopLocationUpdates() {
        this.fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    private fun getNotification(): Notification {
        val intent = Intent(this, MyLocationService::class.java)
        val text = Utils.getLocationText(this.currentLocation)
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true)
        val servicePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val activityPendingIntent = PendingIntent.getActivity(this, 0, Intent(this, HomeActivity::class.java), 0)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .addAction(R.drawable.ic_launch, getString(R.string.launch_activity), activityPendingIntent)
            .addAction(R.drawable.ic_exit, getString(R.string.remove_location_updates), servicePendingIntent)
            .setContentText(text)
            .setContentTitle(Utils.getLocationTitle(this))
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_traveller_icon)
            .setTicker(text)
            .setWhen(System.currentTimeMillis())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder.setChannelId(CHANNEL_ID)

        return builder.build()
    }

    private fun getLastLocation() {
        this.fusedLocationClient?.lastLocation?.addOnSuccessListener {
            this.currentLocation = it
        }
    }

    /**
     * gdy nowa lokalizacja
     * - wyślij broadcast z lokalizacja
     * - uaktuanij powiadomienie
     */
    private fun onNewLocation(location: Location) {
        this.currentLocation = location

        val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_LOCATION, location)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

        if (isRunningInForeground(this)) {
            notificationManager?.notify(
                NOTIFICATION_ID,
                this.getNotification()
            )
        }
    }

    private fun initLocationrequest() {
        locationRequest = LocationRequest()
        locationRequest!!.interval = UPDATE_INTERVAL_MS
        locationRequest!!.fastestInterval = FASTEST_UPDATE_INTERVAL_MS
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * klasa może być też interfejs
     * aktywność która zbinduje ten serwis za pomocą tej klasy może zarządzać ty mserwisem
     *
     * właśnie tą zwróconą wartość dostaje locationServiceConnection w HomeActivity
     */
    inner class LocalBinder : Binder() {
        val service: MyLocationService
            get() = this@MyLocationService
    }

    /**
     * sprawdza w jakim stanie działa serwis
     * czy działa pierwszoplanowa z aktywnym powiadomieniem
     * czy w tle podczas gdy aplikacja jest na pierwszym planie
     */
    private fun isRunningInForeground(context: Context): Boolean {
        val manager = context.getSystemService(
            Context.ACTIVITY_SERVICE
        ) as ActivityManager
        for (runningServiceInfo in manager.getRunningServices(Int.MAX_VALUE)) {
            if (javaClass.name == runningServiceInfo.service.className) {
                if (runningServiceInfo.foreground) {
                    return true
                }
            }
        }
        return false
    }
}