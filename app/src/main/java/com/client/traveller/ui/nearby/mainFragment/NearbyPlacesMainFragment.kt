package com.client.traveller.ui.nearby.mainFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager

import com.client.traveller.R
import com.client.traveller.data.network.api.places.response.nearbySearchResponse.Result
import com.client.traveller.ui.nearby.NearbyPlacesViewModel
import com.client.traveller.ui.nearby.NearbyPlacesViewModelFactory
import com.client.traveller.ui.util.ScopedFragment
import com.client.traveller.ui.util.hideProgressBar
import com.client.traveller.ui.util.showProgressBar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_nearby_places_main.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class NearbyPlacesMainFragment : ScopedFragment(), KodeinAware{

    override val kodein by kodein()
    private val factory: NearbyPlacesViewModelFactory by instance()
    private lateinit var viewModel: NearbyPlacesViewModel

    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>


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

        launch(Dispatchers.Main) {
            this@NearbyPlacesMainFragment.viewModel.updateSearchedPlaces(this@NearbyPlacesMainFragment.viewModel.findNearbyPlaces())
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.bindUI()
    }

    private fun bindUI() = launch(Dispatchers.Main) {
        progress_bar.showProgressBar()
        this@NearbyPlacesMainFragment.viewModel.searchedPlaces.observe(viewLifecycleOwner, Observer { places ->
            progress_bar.showProgressBar()
            this@NearbyPlacesMainFragment.updateItems(places.toNearbyPlaceItems())
            progress_bar.hideProgressBar()
        })
    }

    private fun updateItems(items: List<NearbyPlacesListItem>) {
        this.groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(items)
        }
        recycler_view?.apply {
            layoutManager = LinearLayoutManager(this@NearbyPlacesMainFragment.context)
            adapter = this@NearbyPlacesMainFragment.groupAdapter
        }
    }

    private fun Set<Result>.toNearbyPlaceItems(): List<NearbyPlacesListItem> {
        return this.map {result ->
            if (result.photos != null) {
                val url = viewModel.getPhoto(result.photos.first().photoReference, result.photos.first().width)
                NearbyPlacesListItem(result, view, url)
            } else {
                NearbyPlacesListItem(result, view, null)
            }
        }
    }

}
