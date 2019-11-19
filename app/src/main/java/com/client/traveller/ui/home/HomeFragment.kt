package com.client.traveller.ui.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.R
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.ui.util.ScopedFragment
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class HomeFragment : ScopedFragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: HomeViewModelFactory by instance()
    private lateinit var viewModel: HomeViewModel
    private lateinit var toggle: ActionBarDrawerToggle

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
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        my_location.setOnClickListener { viewModel.centerOnMe() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
