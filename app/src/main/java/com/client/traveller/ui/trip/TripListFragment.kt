package com.client.traveller.ui.trip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.client.traveller.R
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.ui.util.ScopedFragment
import com.client.traveller.ui.util.hideProgressBar
import com.client.traveller.ui.util.showProgressBar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_trip_list.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.util.*


// TODO lista wyceczek
// z prawej strony na dole button + do dodawania wycieczek
// gdy klikniemy wycieczke to pojawia się button do zaakceptowania -> nim też wracamy do homeActivity i tam powinna być wyświetlona ta wycieczka w drawerze
// z prawej strony kazego itemu wycieczki 3 kropki (tj. menu) w menu kopiuj link, edytuj wycieczke, udostępnij link-> deep link
class TripListFragment : ScopedFragment(), KodeinAware, OnItemClickListener {

    override val kodein by kodein()
    private val factory: TripViewModelFactory by instance()
    private lateinit var viewModel: TripViewModel

    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>
    private var currentTrip: Trip? = null
    private var allTrips: List<Trip> = listOf()
    private lateinit var currentUser: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trip_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // jest podawany argument null do TripCreatorFragment spradza czy chemy tworzyć nową wycieczkę
        // czy przeglądamy już stworzoną ( w tym przypadku jako argument jest podawany trip )
        fab.setOnClickListener {
            val action =
                TripListFragmentDirections.actionTripListFragmentToTripCreatorFragment(null, 0)
            Navigation.findNavController(view).navigate(action)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(activity!!, factory).get(TripViewModel::class.java)
        this.viewModel.initAllTripsLiveData()

        viewModel.currentUser.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer

            this.currentUser = it
        })

        viewModel.currentTrip.observe(viewLifecycleOwner, Observer {
            this.currentTrip = it
        })
        this.bindUI()
    }

    override fun onStart() {
        super.onStart()

        // bo jesteśmy na liście, item nie jest kliknięty
        viewModel.selectedItem = null
    }

    private fun bindUI() = launch(Dispatchers.Main) {
        progress_bar.showProgressBar()
        updateHeader(getString(R.string.title_trips_list), null)

        viewModel.allTrips.observe(this@TripListFragment, Observer { trips: List<Trip>? ->
            if (trips == null) return@Observer
            this@TripListFragment.allTrips = trips
            updateTrips(this@TripListFragment.allTrips)
            progress_bar.hideProgressBar()
        })
        this@TripListFragment.viewModel.searchQuery.observe(viewLifecycleOwner, Observer { filtr ->
            val newAllTripsList = mutableListOf<Trip>()
            if (filtr.isEmpty() && this@TripListFragment.allTrips.isNotEmpty()) {
                this@TripListFragment.updateTrips(this@TripListFragment.allTrips)
                progress_bar.hideProgressBar()
                return@Observer
            } else if (this@TripListFragment.allTrips.isNotEmpty()) {
                this@TripListFragment.allTrips.forEach { trip ->
                    if (trip.name?.toLowerCase(Locale.ROOT)?.contains(filtr.toLowerCase(Locale.ROOT))!!)
                        newAllTripsList.add(trip)
                }
                this@TripListFragment.updateTrips(newAllTripsList)
                progress_bar.hideProgressBar()
            }
        })
    }

    private fun List<Trip>.toTripItems(): List<TripListItem> {
        return this.map {
            TripListItem(it, currentTrip, requireContext(), viewModel, currentUser)
        }
    }

    private fun updateTrips(trips: List<Trip>) {
        val tripItems = trips.toTripItems()
        groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(tripItems)
        }
        groupAdapter.setOnItemClickListener(this)
        recycler_view?.apply {
            layoutManager = LinearLayoutManager(this@TripListFragment.context)
            adapter = groupAdapter
        }
    }


    private fun updateHeader(title: String, subtitle: String?) {
        (activity as? AppCompatActivity)?.supportActionBar?.title = title

        (activity as? AppCompatActivity)?.supportActionBar?.subtitle = subtitle
    }

    override fun onItemClick(item: Item<*>, view: View) {
        if (item is TripListItem) {
            viewModel.selectedItem = item
            val action = TripListFragmentDirections.actionTripListFragmentToTripCreatorFragment(
                item.trip,
                view.id
            )
            this.view?.findNavController()?.navigate(action)
        }

    }
}
