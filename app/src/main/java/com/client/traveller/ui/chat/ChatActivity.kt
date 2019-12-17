package com.client.traveller.ui.chat

import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.client.traveller.ui.BaseActivity
import com.client.traveller.R
import com.client.traveller.ui.about.AboutActivity
import com.client.traveller.ui.auth.AuthActivity
import com.client.traveller.ui.chat.chatList.ChatListFragment
import com.client.traveller.ui.chat.usersList.TripUsersFragment
import com.client.traveller.ui.home.HomeActivity
import com.client.traveller.ui.nearby.NearbyPlacesActivity
import com.client.traveller.ui.settings.SettingsActivity
import com.client.traveller.ui.trip.TripActivity
import com.client.traveller.ui.tripInfo.TripInfoActivity
import com.client.traveller.ui.util.Coroutines
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.nav_header.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class ChatActivity : BaseActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: ChatViewModelFactory by instance()
    private lateinit var viewModel: ChatViewModel
    private var doubleBack = false

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolBar: androidx.appcompat.widget.Toolbar
    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        viewModel = ViewModelProvider(this, factory).get(ChatViewModel::class.java)

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
        this.supportActionBar?.title = getString(R.string.chat_title)
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

        // inicjalizacja zakładek
        this.viewPager = view_pager
        this.tabLayout = tab_layout
        this.viewPagerAdapter = ViewPagerAdapter(this)
        this.viewPagerAdapter.addFragment(ChatListFragment())
        this.viewPagerAdapter.addFragment(TripUsersFragment())
        this.viewPager.adapter = this.viewPagerAdapter
//        this.tabLayout.setupWithViewPager(this.viewPager)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.list)
                1 -> tab.text = getString(R.string.participants)
            }
        }.attach()

        // inicjalizacja dolengo paska nwigacji
        this.bottomNavigation = bottom_navigation
        this.bottomNavigation.selectedItemId = R.id.chat
        this.bottomNavigation.setOnNavigationItemSelectedListener(onBottomNavigationItemSelected)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val menuItem = menu?.findItem(R.id.search_menu)
        this.searchView = menuItem?.actionView as SearchView
        this.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                this@ChatActivity.viewModel.searchQuery.value = newText
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    private fun setSubtitleNavView(subtitle: String) = Coroutines.main {
        navigationView.getHeaderView(0).subtitle.text = subtitle
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
                    Intent(this, TripInfoActivity::class.java).also {
                        startActivity(it)
                        this.finish()
                    }
                }
                R.id.map_place_details -> {
                    Intent(this, HomeActivity::class.java).also {
                        startActivity(it)
                        this.finish()
                    }
                }
                R.id.nearby -> {
                    Intent(this, NearbyPlacesActivity::class.java).also {
                        startActivity(it)
                        this.finish()
                    }
                }
            }
            true
        }

    override fun onNewLocation(location: Location) {}

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
