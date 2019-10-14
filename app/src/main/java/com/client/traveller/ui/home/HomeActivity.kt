package com.client.traveller.ui.home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.client.traveller.BuildConfig
import com.client.traveller.R
import com.client.traveller.data.provider.PlacesClientProvider
import com.client.traveller.data.services.UploadService
import com.client.traveller.ui.about.AboutActivity
import com.client.traveller.ui.auth.AuthActivity
import com.client.traveller.ui.dialog.Dialog
import com.client.traveller.ui.settings.SettingsActivity
import com.client.traveller.ui.trip.TripActivity
import com.client.traveller.ui.util.Coroutines
import com.client.traveller.ui.util.hideLoding
import com.client.traveller.ui.util.showLoading
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.paulrybitskyi.persistentsearchview.PersistentSearchView
import com.paulrybitskyi.persistentsearchview.utils.SuggestionCreationUtil
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.nav_header.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import kotlin.coroutines.suspendCoroutine

class HomeActivity : AppCompatActivity(),
    KodeinAware, NavigationView.OnNavigationItemSelectedListener {

    override val kodein by kodein()
    private val factory: HomeViewModelFactory by instance()
    private lateinit var viewModel: HomeViewModel
    private var doubleBack = false

    private lateinit var searchView: PersistentSearchView
    private lateinit var drawer: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        viewModel.getLoggedInUser().observe(this, Observer { user ->
            if (user != null) {
                this.setSubtitleNavView(user.email!!)
            }
        })

        if (intent != null)
            this.getDynamicLinks()

        drawer = drawer_layout
        searchView = persistentSearchView
        initSearchView()
        navigation_view.setNavigationItemSelectedListener(this)

    }

    /**
     * Inicjalizacja [searchView]
     */
    private fun initSearchView() {
        searchView.hideRightButton()
        searchView.setVoiceInputButtonDrawable(null)

        searchView.setOnLeftBtnClickListener {
            if (!this.drawer.isDrawerOpen(Gravity.LEFT))
                drawer.openDrawer(Gravity.LEFT)
            else
                drawer.closeDrawer(Gravity.LEFT)
        }

        searchView.setOnSearchConfirmedListener { searchView, query ->
            //TODO momżna wybierać pierwszy rezulatat
        }
        searchView.setOnSearchQueryChangeListener { _, _, newQuery ->
            if (newQuery.isNotEmpty()) {
                this.searchView.showLoading()
                performSearch(newQuery)
            }
        }
    }

    private fun setSubtitleNavView(subtitle: String) = Coroutines.main {
        navigation_view.getHeaderView(0).subtitle.text = subtitle
    }

    /**
     * Pobranie i wpisanie wyników do sugestii searchview
     * @param query zapytanie z searchview
     */
    private fun performSearch(query: String) = Coroutines.main {
        val suggestions =
            SuggestionCreationUtil.asRegularSearchSuggestions(this@HomeActivity.search(query))
        this@HomeActivity.searchView.hideLoding()
        persistentSearchView.setSuggestions(suggestions, true)
    }

    /**
     * Pobranie wyników z [PlacesClientProvider] na podstawie [query]
     * @param query zapytanie z searchview
     */
    private suspend fun search(query: String): List<String> {
        val placesClient = PlacesClientProvider.getClient(this)
        val token = AutocompleteSessionToken.newInstance()
        var predictionList: List<AutocompletePrediction>
        var suggestionList: List<String>
        val telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val predictionsRequest = FindAutocompletePredictionsRequest.builder()
            .setCountry(telephonyManager.networkCountryIso)
            .setTypeFilter(TypeFilter.ADDRESS)
            .setSessionToken(token)
            .setQuery(query)
            .build()

        return suspendCoroutine { continuation ->
            placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener {
                if (it.isSuccessful) {
                    val predictionsResponse = it.result
                    if (predictionsResponse != null) {
                        predictionList = predictionsResponse.autocompletePredictions
                        suggestionList = predictionList.map { prediction ->
                            prediction.getFullText(null).toString()
                        }
                        continuation.resumeWith(Result.success(suggestionList))
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Sprawdza czy aktywność zotała uruchomiona kliknięciem na powiadomienie z wysyłania pliku
        if (intent.hasExtra(UploadService.EXTRA_DOWNLOAD_URL)) {
//            onUploadResultIntent(intent)
            Log.e(javaClass.simpleName, "powiadomienie!")
        }
    }

    override fun onBackPressed() {
        if (drawer_layout != null && drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
            return
        } else if (doubleBack) {
            super.onBackPressed()
            return
        }

        this.doubleBack = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBack = false }, 2000)
    }

    /**
     * Uprawnienia lokalizacji są także sprawdzane w [LocationProviderImpl] z kodem 121
     * tutaj jest sprawdzane czy te uprawnienia zostały przyznane
     * jeżeli tak to zostaje wywołana metoda startLocationUpdates
     * jeżeli nie zostają otworzone ustawienia gdzie możemy zmienić uprawnienia [openSettings]
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            121 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.startLocationUpdates()
                } else {
                    openSettings()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

        if (viewModel.sendingLocationData()) {
            viewModel.startLocationUpdates()
        }

    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.ustawienia_item -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.trips -> {
                Intent(this, TripActivity::class.java).also {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(it)
                }
            }
            R.id.about -> {
                Intent(this, AboutActivity::class.java).also {
                    startActivity(it)
                }
            }
            R.id.logout -> {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

                viewModel.logoutUser(mGoogleSignInClient)
                Intent(this, AuthActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            }
            R.id.profile -> {
                Navigation.findNavController(this, R.id.nav_host_fragment_home).navigate(R.id.profileFragment)
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * wywoływana gdy aktywność przejdzie na drugi plan ( zakryje ją inna aktywność )
     */
    override fun onPause() {
        super.onPause()

        if (viewModel.sendingLocationData()) {
            viewModel.stopLocationUpdates()
        }
    }

    //TODO trzeba pprawić metodę
    private fun getDynamicLinks() {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) {
                var deepLink: Uri? = null

                if (it != null) {
                    Log.e(
                        javaClass.simpleName,
                        "dynamic Link working!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
                    )
//                    FirebaseAuth.getInstance().applyActionCode()
                    deepLink = it.link
                    viewModel.setEmailVerified()
                    Dialog.Builder()
                        .addMessage(getString(R.string.post_email_verification_success))
                        .addPositiveButton("Ok") { dialog ->
                            dialog.dismiss()
                        }
                        .build(supportFragmentManager, javaClass.simpleName)
                } else {
                    if (intent.data != null) {
                        this.handleLink(intent.data)
                    }
                }
            }
            .addOnFailureListener {
                Log.e(javaClass.simpleName, it.localizedMessage)
            }
    }

    //https://travellersystems.page.link?
// link=https://traveller-249409.firebaseapp.com/__/auth/action?
// apiKey%3DAIzaSyCrwQqjOn5v4BDdkKCZHMmmav1YEzvaq5s%26mode%3D
// verifyEmail%26oobCode%3DL5SpczQtw2sKuIaCPOu8s9CFcziz3Cdmo1KB9JLWY5UAAAFtJizsbA%26
// continueUrl%3Dhttps://travellersystems.page.link/verify?email%253Ddaniellegawiec20@gmail.com%26lang%3Dpl&
// apn=com.client.traveller&amv
    private fun handleLink(uri: Uri?) {

        if (uri == null)
            return

        val link = Uri.parse(uri.getQueryParameter("link"))

        var mode = link.getQueryParameter("mode")
        var actionCode = link.getQueryParameter("oobCode")
        var apiKey = link.getQueryParameter("apiKey")
        var continueUrl = link.getQueryParameter("continueUrl")

        if (mode == "verifyEmail" && actionCode != null) {
            viewModel.setEmailVerified()
            FirebaseAuth.getInstance().applyActionCode(actionCode)
                .addOnSuccessListener {
                    Dialog.Builder()
                        .addMessage(getString(R.string.post_email_verification_success))
                        .addPositiveButton("Ok") { dialog ->
                            dialog.dismiss()
                        }
                        .build(supportFragmentManager, javaClass.simpleName)
                }
                .addOnFailureListener {
                    Dialog.Builder()
                        .addTitle(getString(R.string.post_email_verification_fail))
                        .addMessage(it.localizedMessage)
                        .addPositiveButton("Ok") { dialog ->
                            dialog.dismiss()
                        }
                        .build(supportFragmentManager, javaClass.simpleName)
                }

        }
    }

}
