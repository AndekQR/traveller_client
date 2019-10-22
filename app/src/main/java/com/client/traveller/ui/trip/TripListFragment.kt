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
import com.client.traveller.ui.util.ScopedFragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
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
class TripListFragment : ScopedFragment(), KodeinAware, OnItemClickListener {

    override val kodein by kodein()
    private val factory: TripViewModelFactory by instance()
    private lateinit var viewModel: TripViewModel

    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>

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
            val action = TripListFragmentDirections.actionTripListFragmentToTripCreatorFragment(null)
            Navigation.findNavController(view).navigate(action)
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
            TripListItem(it, requireContext(), viewModel)
        }
    }

    private fun updateTrips(trips: List<Trip>) {
        groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(trips.toTripItems())
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
        if (item is TripListItem){
            val action = TripListFragmentDirections.actionTripListFragmentToTripCreatorFragment(item.trip)
            this.view?.findNavController()?.navigate(action)
        }

    }
}
