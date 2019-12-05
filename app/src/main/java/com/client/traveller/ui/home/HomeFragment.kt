package com.client.traveller.ui.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.client.traveller.R
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.ui.util.ScopedFragment
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class HomeFragment : ScopedFragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: HomeViewModelFactory by instance()
    private lateinit var viewModel: HomeViewModel
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var currentTrip: Trip

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = activity?.run {
            ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        } ?: throw Exception("Invalid activity")

        // inicjalizacja mapy oraz jej funkcji
        // childFragmentManager służy do zarządzania fragmentami w tym fagmencie
        // a fragmentManager do zarządzania fragmentami które są związane z activity tego fragmentu
        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)?.let {
            activity?.let { activity ->
                viewModel.initMap(it, activity, savedInstanceState)
            }
        }

        this.viewModel.currentTrip.observe(viewLifecycleOwner, Observer { trip ->
            if (trip == null) return@Observer
            launch {
                this@HomeFragment.viewModel.drawTripRoute(trip)
            }
            this.currentTrip = trip
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        my_location.setOnClickListener { viewModel.centerOnMe() }
        clear_button.setOnClickListener { this.viewModel.clearMap() }
        center_road_button.setOnClickListener {
            if (::currentTrip.isInitialized)
                lifecycleScope.launch(Dispatchers.Main) {
                    this@HomeFragment.viewModel.centerRoad(this@HomeFragment.currentTrip.startAddress!!, this@HomeFragment.currentTrip.waypoints, this@HomeFragment.currentTrip.endAddress!!)
                }
        }
        search_nearby_places_button.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                search_nearby_places_button.visibility = View.INVISIBLE
                search_nearby_places_progress_bar.visibility = View.VISIBLE
                this@HomeFragment.viewModel.drawMarkerNearbyPlaces()
                search_nearby_places_progress_bar.visibility = View.GONE
                search_nearby_places_button.visibility = View.VISIBLE
            }
        }
        draw_route_button.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                draw_route_button.visibility = View.INVISIBLE
                draw_route_button_progress_bar.visibility = View.VISIBLE
                this@HomeFragment.viewModel.drawRouteToMainMarker()
                draw_route_button.visibility = View.VISIBLE
                draw_route_button_progress_bar.visibility = View.GONE
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}
