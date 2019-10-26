package com.client.traveller.ui.chat

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.viewpager.widget.ViewPager
import com.client.traveller.R
import com.client.traveller.ui.about.AboutActivity
import com.client.traveller.ui.auth.AuthActivity
import com.client.traveller.ui.home.HomeActivity
import com.client.traveller.ui.settings.SettingsActivity
import com.client.traveller.ui.trip.TripActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_chat.*

import kotlinx.android.synthetic.main.activity_chat.drawer_layout
import kotlinx.android.synthetic.main.activity_chat.navigation_view

import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class ChatActivity : AppCompatActivity(), KodeinAware{

    override val kodein by kodein()
    private val factory: ChatViewModelFactory by instance()
    private lateinit var viewModel: ChatViewModel
    private var doubleBack = false

    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolBar: androidx.appcompat.widget.Toolbar
    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        viewModel = ViewModelProvider(this, factory).get(ChatViewModel::class.java)

        // inicjalizacja drawera
        this.drawerLayout = drawer_layout
        this.navigationView = navigation_view
        this.navigationView.setNavigationItemSelectedListener(onNavigationItemSelected)

        // inicjalizacja actionbara
        this.toolBar = toolbar
        this.setSupportActionBar(this.toolBar)
        this.supportActionBar?.title = getString(R.string.chat_title)
        this.mDrawerToggle = ActionBarDrawerToggle(this, this.drawerLayout, this.toolBar, R.string.open_drawer, R.string.close_drawer)
        this.drawerLayout.addDrawerListener(mDrawerToggle)
        this.mDrawerToggle.syncState()
        this.mDrawerToggle.isDrawerIndicatorEnabled = true
        this.supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // inicjalizacja zakładek
        this.viewPager = view_pager
        this.tabLayout = tab_layout
        this.viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        this.viewPagerAdapter.addFragment(ChatFragment(), getString(R.string.list))
        this.viewPagerAdapter.addFragment(TripUsersFragment(), getString(R.string.participants))
        this.viewPager.adapter = this.viewPagerAdapter
        this.tabLayout.setupWithViewPager(this.viewPager)

        // inicjalizacja dolengo paska nwigacji
        this.bottomNavigation = bottom_navigation
        this.bottomNavigation.selectedItemId = R.id.chat
        this.bottomNavigation.setOnNavigationItemSelectedListener(onBottomNavigationItemSelected)

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
                Navigation.findNavController(this, R.id.nav_host_fragment_home)
                    .navigate(R.id.profileFragment)
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        true
    }
    private val onBottomNavigationItemSelected =  BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when(item.itemId) {
            R.id.trip -> {
            }
//            R.id.chat -> {
//                Intent(this, ChatActivity::class.java).also {
//                    startActivity(it)
//                }
//            }
            R.id.map -> {
//                Navigation.findNavController(this, R.id.nav_host_fragment_home).navigate(R.id.homeFragment)
                Intent(this, HomeActivity::class.java).also {
                    startActivity(it)
                }
            }
            R.id.nearby -> {

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
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBack = false }, 2000)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        this.mDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        this.mDrawerToggle.onConfigurationChanged(newConfig)
    }
}
