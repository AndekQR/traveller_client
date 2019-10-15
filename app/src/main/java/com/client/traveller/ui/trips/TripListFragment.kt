package com.client.traveller.ui.trips

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.client.traveller.R
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.ui.util.ScopedFragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_trip_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


// TODO lista wyceczek
// z prawej strony na dole button + do dodawania wycieczek
// gdy klikniemy wycieczke to pojawia się button do zaakceptowania -> nim też wracamy do homeActivity i tam powinna być wyświetlona ta wycieczka w drawerze
// z prawej strony kazego itemu wycieczki 3 kropki (tj. menu) w menu kopiuj link, edytuj wycieczke, udostępnij link-> deep link
class TripListFragment : ScopedFragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: TripViewModelFactory by instance()
    private lateinit var viewModel: TripViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trip_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.tripCreatorFragment)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this, factory).get(TripViewModel::class.java)

        this.bindUI()
    }

    private fun bindUI() = launch(Dispatchers.Main) {
        updateHeader(getString(R.string.title_trips_list), null)

        viewModel.getAllTrips().observe(this@TripListFragment, Observer { trips: List<Trip>? ->
            if (trips == null) return@Observer
            updateTrips(trips)
        })

    }

    private fun List<Trip>.toTripItems(): List<TripListItem> {
        return this.map {
            TripListItem(it)
        }
    }

    private fun updateTrips(trips: List<Trip>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(trips.toTripItems())
        }

        recycler_view?.apply {
            layoutManager = LinearLayoutManager(this@TripListFragment.context)
            adapter = groupAdapter
        }
    }

    private fun updateHeader(title: String, subtitle: String?) {
        (activity as? AppCompatActivity)?.supportActionBar?.title = title

        (activity as? AppCompatActivity)?.supportActionBar?.subtitle = subtitle
    }
}
