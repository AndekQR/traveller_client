package com.client.traveller.ui.nearby

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.R
import com.client.traveller.ui.about.AboutActivity
import com.client.traveller.ui.auth.AuthActivity
import com.client.traveller.ui.chat.ChatActivity
import com.client.traveller.ui.home.HomeActivity
import com.client.traveller.ui.settings.SettingsActivity
import com.client.traveller.ui.trip.TripActivity
import com.client.traveller.ui.util.Coroutines
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_nearby_places.*
import kotlinx.android.synthetic.main.nav_header.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class NearbyPlaces : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: NearbyPlacesViewModelFactory by instance()
    private lateinit var viewModel: NearbyPlacesViewModel
    private var doubleBack = false

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolBar: androidx.appcompat.widget.Toolbar
    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_places)

        this.viewModel = ViewModelProvider(this, factory).get(NearbyPlacesViewModel::class.java)
        this.initView()
    }

    private fun initView() {
        // inicjalizacja drawera
        this.drawerLayout = drawer_layout
        this.navigationView = navigation_view
        this.navigationView.setNavigationItemSelectedListener(onNavigationItemSelected)
        this.viewModel.currentUser.observe(this, Observer { user ->
            this.setSubtitleNavView(user.email!!)
        })

        // inicjalizacja actionbara
        this.toolBar = toolbar
        this.setSupportActionBar(this.toolBar)
        this.supportActionBar?.title = getString(R.string.nearby)
        this.mDrawerToggle = ActionBarDrawerToggle(
            this,
            this.drawerLayout,
            this.toolBar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        this.drawerLayout.addDrawerListener(mDrawerToggle)
        this.mDrawerToggle.syncState()
        this.mDrawerToggle.isDrawerIndicatorEnabled = true
        this.supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // inicjalizacja dolengo paska nwigacji
        this.bottomNavigation = bottom_navigation
        this.bottomNavigation.selectedItemId = R.id.nearby
        this.bottomNavigation.setOnNavigationItemSelectedListener(onBottomNavigationItemSelected)
    }

    private fun setSubtitleNavView(subtitle: String) = Coroutines.main {
        this.navigationView.getHeaderView(0).subtitle.text = subtitle
    }

    private val onNavigationItemSelected = NavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
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
                val gso =
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

                viewModel.logoutUser(mGoogleSignInClient)
                Intent(this, AuthActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            }
            R.id.profile -> {
                Intent(this, HomeActivity::class.java).also {
                    it.putExtra("frag", "profile")
                    startActivity(it)
                    this.finish()
                }
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        true
    }
    private val onBottomNavigationItemSelected =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.trip -> {

                }
                R.id.map -> {
                    Intent(this, HomeActivity::class.java).also {
                        startActivity(it)
                        this.finish()
                    }
                }
                R.id.chat -> {
                    Intent(this, ChatActivity::class.java).also {
                        startActivity(it)
                        this.finish()
                    }
                }
            }
            true
        }

    override fun onBackPressed() {
        if (doubleBack) {
            super.onBackPressed()
            return
        }

        this.doubleBack = true
        Toast.makeText(this, getString(R.string.click_back_again_to_exit), Toast.LENGTH_SHORT)
            .show()
        Handler().postDelayed({ doubleBack = false }, 2000)
    }
}
