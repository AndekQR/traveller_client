package com.client.traveller.ui.nearby.mainFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.client.traveller.R
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
import com.client.traveller.ui.nearby.NearbyPlacesViewModel
import com.client.traveller.ui.nearby.NearbyPlacesViewModelFactory
import com.client.traveller.ui.util.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_nearby_places_main.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class NearbyPlacesMainFragment : ScopedFragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: NearbyPlacesViewModelFactory by instance()
    private lateinit var viewModel: NearbyPlacesViewModel

    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>

    private var searchedPlaces = setOf<Result>()
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nearby_places_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = activity?.run {
            ViewModelProvider(this, factory).get(NearbyPlacesViewModel::class.java)
        } ?: throw Exception("Invalid activity")

        this.bindUI()

        swipe_to_refresh_layout.setOnRefreshListener {
            this.viewModel.updatePlaces()
            swipe_to_refresh_layout.isRefreshing = false
        }
    }


    private fun bindUI() {
        progress_bar.showProgressBar()
        this@NearbyPlacesMainFragment.viewModel.searchedPlaces.observe(
            viewLifecycleOwner,
            Observer { places: Set<Result>? ->
                if (places == null) return@Observer
                // nowe miejsca są takie same -> nie ma co aktualizować
                if (this@NearbyPlacesMainFragment.searchedPlaces.isNotEmpty() &&
                            places.isNotEmpty() &&
                            places == this@NearbyPlacesMainFragment.searchedPlaces
                ) {
                    return@Observer
                }
                if (places.isEmpty()) {
                    nothing_to_display.visibility = View.VISIBLE
                    recycler_view.visibility = View.GONE
                    progress_bar.hideProgressBar()
                } else {
                    recycler_view.visibility = View.VISIBLE
                    nothing_to_display.visibility = View.GONE
                    this@NearbyPlacesMainFragment.viewModel.initOriginalListOfPlaces(places)
                    this@NearbyPlacesMainFragment.searchedPlaces = places
                    this@NearbyPlacesMainFragment.updateItems(places.toNearbyPlaceItems())
                    progress_bar.hideProgressBar()
                }
            })
//        this@NearbyPlacesMainFragment.viewModel.updatePlaces()
    }

    private fun updateItems(items: List<NearbyPlacesListItem>) = launch(Dispatchers.Main) {
        this@NearbyPlacesMainFragment.groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(items)
        }
        this@NearbyPlacesMainFragment.groupAdapter.setOnItemClickListener(this@NearbyPlacesMainFragment.onItemClickListener)
        recycler_view?.apply {
            layoutManager = LinearLayoutManager(this@NearbyPlacesMainFragment.context)
            adapter = this@NearbyPlacesMainFragment.groupAdapter
        }
    }

    private fun Set<Result>.toNearbyPlaceItems(): List<NearbyPlacesListItem> {
        return this.map { result ->
            if (result.photos != null) {
                val url = viewModel.getPhoto(
                    result.photos.first().photoReference,
                    result.photos.first().width
                )
                NearbyPlacesListItem(result, view, url, context)
            } else {
                NearbyPlacesListItem(result, view, null, context)
            }
        }
    }

    private val onItemClickListener = OnItemClickListener { item, view ->
        if (item is NearbyPlacesListItem) {
            val action =
                NearbyPlacesMainFragmentDirections.actionNearbyPlacesMainFragmentToPlaceDetailFragment(
                    item.getPlaceId()
                )
            Navigation.findNavController(view).navigate(action)
        }
    }

}
