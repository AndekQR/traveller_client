package com.client.traveller.ui.nearby

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.R
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
import com.client.traveller.ui.about.AboutActivity
import com.client.traveller.ui.auth.AuthActivity
import com.client.traveller.ui.chat.ChatActivity
import com.client.traveller.ui.home.HomeActivity
import com.client.traveller.ui.settings.SettingsActivity
import com.client.traveller.ui.trip.TripActivity
import com.client.traveller.ui.util.Coroutines
import com.client.traveller.ui.util.ScopedAppActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_nearby_places.*
import kotlinx.android.synthetic.main.nav_header.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.*

class NearbyPlaces : ScopedAppActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: NearbyPlacesViewModelFactory by instance()
    private lateinit var viewModel: NearbyPlacesViewModel
    private var doubleBack = false

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolBar: androidx.appcompat.widget.Toolbar
    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    private lateinit var currentListOfPlaces: Set<Result>
    private var searchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_places)

        this.viewModel = ViewModelProvider(this, factory).get(NearbyPlacesViewModel::class.java)

        this@NearbyPlaces.viewModel.searchedPlaces.observe(this@NearbyPlaces, Observer { places ->
            if (places == null) return@Observer
            if(::currentListOfPlaces.isInitialized && places == currentListOfPlaces) return@Observer
            this.currentListOfPlaces = places
            this.viewModel.initOriginalListOfPlaces(places)
            this.observeSearchQuery()

        })
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        menuInflater.inflate(R.menu.nearby_places_menu, menu)

        val searchViewItem = menu?.findItem(R.id.search_menu)
        val searchView = searchViewItem?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                this@NearbyPlaces.viewModel.searchQuery.value = newText
                return true
            }

        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.filter_places -> {
            this.showMultichooseDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showMultichooseDialog() {
        val values = this.viewModel.getPlacesSearchedTypes()
        val names = values.map {
            val id = resources.getIdentifier(it, "string", packageName)
            if (id != 0) return@map getString(id)
            else ""
        }.filter { it.isNotEmpty() }

        val builder = AlertDialog.Builder(this)
            .setTitle(getString(R.string.places_types))

        val loclCheckedItems = viewModel.checkedItems
        builder.setMultiChoiceItems(
            names.toTypedArray(),
            viewModel.checkedItems.toBooleanArray()
        ) { _, which: Int, isChecked: Boolean ->
            loclCheckedItems[which] = isChecked
        }

        builder.setPositiveButton("ok") { dialog, _ ->
            this.viewModel.checkedItems = loclCheckedItems
            this.filterPlaces(this.searchQuery)
            dialog.dismiss()
        }
        builder.create().show()

    }

    private fun observeSearchQuery() {
        this.viewModel.searchQuery.observe(this, Observer { searchQuery ->
            if (searchQuery == null) return@Observer
            this.searchQuery = searchQuery
            this.filterPlaces(searchQuery)
        })
    }

    private fun filterPlaces(searchQuery: String) {
        val filteredByTypes = this.viewModel.getOriginalListOfPlaces().filtrPlacesByTypes()
        val filteredByTypesAndQuery = filteredByTypes.filterByQuery(searchQuery)
        this.viewModel.updateSearchedPlaces(filteredByTypesAndQuery)
    }

    private fun Set<Result>.filterByQuery(searchQuery: String): Set<Result> {
        if (searchQuery.isNotEmpty()){
            val filteredPlaces = this.filter {
                it.name.toLowerCase(Locale.ROOT).contains(searchQuery.toLowerCase(Locale.ROOT)) ||
                        it.vicinity.toLowerCase(Locale.ROOT).contains(searchQuery.toLowerCase(
                            Locale.ROOT))
            }.toSet()
            if (filteredPlaces != this) {
                return filteredPlaces
            }
        } else {
            return this
        }
        return this
    }

    private fun Set<Result>.filtrPlacesByTypes(): Set<Result>  {
        val checkedTypesValues = mutableListOf<String>()
        for ((position, value) in this@NearbyPlaces.viewModel.getPlacesSearchedTypes().withIndex()) {
            if (this@NearbyPlaces.viewModel.checkedItems[position]) {
                checkedTypesValues.add(value)
            }
        }
        val filteredPlaces = this.filter { result ->
            result.types.containsAll(checkedTypesValues)
        }
        return if (filteredPlaces.toSet() != this)
            filteredPlaces.toSet()
        else
            this
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
