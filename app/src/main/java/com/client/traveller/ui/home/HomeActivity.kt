package com.client.traveller.ui.home

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.BuildConfig
import com.client.traveller.R
import com.client.traveller.ui.auth.LoginActivity
import com.client.traveller.ui.dialogs.Dialog
import com.client.traveller.ui.settings.SettingsActivity
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import kotlinx.android.synthetic.main.action_bar.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.nav_header.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    KodeinAware {

    private lateinit var toggle: ActionBarDrawerToggle
    override val kodein by kodein()
    private val factory: HomeViewModelFactory by instance()
    private lateinit var viewModel: HomeViewModel
    private var doubleBack = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        floating_search_view.setOnQueryChangeListener { oldQuery, newQuery ->
            //get suggestions based on newQuery
            //https://github.com/arimorty/floatingsearchview/blob/master/sample/src/main/java/com/arlib/floatingsearchviewdemo/data/DataHelper.java
            //pass them on to the search view
            var lista = listOf<MySearchSuggestion>()
            floating_search_view.swapSuggestions(lista)
        }
        floating_search_view.attachNavigationDrawerToMenuButton(drawer_layout)
        navigation_view.setNavigationItemSelectedListener(this)

        viewModel.getLoggedInUser().observe(this, Observer { user ->
            if (user != null) {
                setSubtitleNavView(user.email!!)
            }
        })

        val map = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        viewModel.initLocationProvider(map, this, savedInstanceState)

        if (intent != null)
            this.getDynamicLinks()

    }


    private fun setSubtitleNavView(subtitle: String) = GlobalScope.launch(Dispatchers.Main) {
        navigation_view.getHeaderView(0).subtitle.text = subtitle
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.ustawienia_item -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.tworca_item -> {
//                val intent = Intent(this, AuthorActivity::class.java)
//                startActivity(intent)
            }
            R.id.logout -> {
                viewModel.logoutUser()
                Intent(this, LoginActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

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

    /**
     * wywoływana po ponownym stworzeniu aktywności
     */
    override fun onResume() {
        super.onResume()

        if (viewModel.sendingLocationData()) {
            viewModel.startLocationUpdates()
        }
    }


    /**
     * wywoływana gdy aktywność przejdzie na drugi plan
     */
    override fun onPause() {
        super.onPause()

        if (viewModel.sendingLocationData()) {
            viewModel.stopLocationUpdates()
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
                    Dialog.newInstance(getString(R.string.post_email_verification_success))
                        .show(this.supportFragmentManager, "Weryfikacja e-mail")
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
    private fun handleLink(uri: Uri) {

        val link = Uri.parse(uri.getQueryParameter("link"))

        var mode = link.getQueryParameter("mode")
        var actionCode = link.getQueryParameter("oobCode")
        var apiKey = link.getQueryParameter("apiKey")
        var continueUrl = link.getQueryParameter("continueUrl")

        if (mode == "verifyEmail" && actionCode != null){
            viewModel.setEmailVerified()
            FirebaseAuth.getInstance().applyActionCode(actionCode)
            Dialog.newInstance(getString(R.string.post_email_verification_success))
                .show(this.supportFragmentManager, "Weryfikacja e-mail")
        }
        else{
            Dialog.newInstance(getString(R.string.post_email_verification_fail))
                .show(this.supportFragmentManager, "Weryfikacja e-mail")
        }
    }

}
