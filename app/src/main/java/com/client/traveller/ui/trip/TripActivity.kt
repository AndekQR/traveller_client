package com.client.traveller.ui.trip

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.client.traveller.R
import com.client.traveller.data.db.entities.User
import kotlinx.android.synthetic.main.activity_trip.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

// TODO trzeba sprawdzać czywycieczka o tej samej nazwie już istnieje i pytać czy nadpisać
class TripActivity : AppCompatActivity(), KodeinAware, NavController.OnDestinationChangedListener {

    override val kodein by kodein()
    private val factory: TripViewModelFactory by instance()
    private lateinit var viewModel: TripViewModel

    private var joinTripItem: MenuItem? = null
    private var searchViewItem: MenuItem? = null
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    private lateinit var currentUser: User
    private var currentFragment: NavDestination? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip)

        viewModel = ViewModelProvider(this, factory).get(TripViewModel::class.java)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        this.navHostFragment = nav_host_fragment_trip as NavHostFragment
        this.navController = this.navHostFragment.navController
        this.navController.addOnDestinationChangedListener(this)

        viewModel.currentUser.observe(this, Observer {
            if (it == null) return@Observer
            this.currentUser = it
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        menuInflater.inflate(R.menu.trip_actionbar_menu, menu)

        this.searchViewItem = menu?.findItem(R.id.search_menu)
        val searchView = this.searchViewItem?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                this@TripActivity.viewModel.searchQuery.value = newText
                return true
            }
        })

        joinTripItem = menu?.findItem(R.id.join_trip)
        joinTripItem?.isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.join_trip -> {
            val creatorFragment: Fragment? =
                this.navHostFragment.childFragmentManager.fragments.first()
            if (creatorFragment != null && creatorFragment is TripCreatorFragment)
                creatorFragment.jointTripButtonClick()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }


    /**
     * jest wywoływane przed OnCreateOptionsMenu
     * Jeżeli jesteśmy w [TripCreatorFragment] przycisk dołącz do wycieczki jest widoczny
     * Potem [TripCreatorFragment] sprawdza czy użytkowwnik jest uczestnikiem jeśli tak to wywołuje metodę [makeJoinTripButtonInvisible] przez callbacck
     */
    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        this.changeTitle(destination.id)
        this.currentFragment = destination
        if (destination.id == R.id.tripCreatorFragment && viewModel.selectedItem != null) {
            joinTripItem?.isVisible =
                !viewModel.isTripParticipant(viewModel.selectedItem!!.trip, this.currentUser)
        } else {
            joinTripItem?.isVisible = false
        }
        this.searchViewItem?.isVisible = destination.id == R.id.tripListFragment
    }

    private fun changeTitle(id: Int) {
        when (id) {
            R.id.tripListFragment -> title = "Wycieczki"
            R.id.tripCreatorFragment -> title = "Wycieczka"
        }
    }

}
