package com.client.traveller.ui.tripInfo

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.client.traveller.R
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.ui.dialog.Dialog
import com.client.traveller.ui.home.HomeActivity
import com.client.traveller.ui.util.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.android.synthetic.main.trip_info_fragment.*
import kotlinx.android.synthetic.main.trip_info_recyclerview_dragable_item.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.util.*


class TripInfoFragment : Fragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: TripInfoViewModelFactory by instance()
    private lateinit var viewModel: TripInfoViewModel

    private lateinit var tripName: TextView
    private lateinit var tripTime: TextView
    private lateinit var tripStartLocation: TextView
    private lateinit var tripEndLocation: TextView
    private  var tripWaypointsRecyclerView: RecyclerView? = null
    private  var groupAdapter: GroupAdapter<GroupieViewHolder>? = null
    private lateinit var anotherPlacesLayout: LinearLayout

    private lateinit var currentUser: User
    private lateinit var currentTrip: Trip
    private var dragableItems = mutableListOf<DragableItem>()
    private val anotherPlaces = mutableListOf<Pair<ListView, ArrayAdapter<String>>>()
    private val anotherPlacesTetView = mutableListOf<TextView>()


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        this.viewModel = ViewModelProvider(activity!!, factory).get(TripInfoViewModel::class.java)

        this.viewModel.currentUser.observe(viewLifecycleOwner, Observer { user ->
            if (user == null) return@Observer
            this.currentUser = user

            this.viewModel.currentTrip.observe(viewLifecycleOwner, Observer { trip ->
                if (trip == null) {
                    cards.visibility = View.GONE
                    no_trip_layout.visibility = View.VISIBLE
                    return@Observer
                }
                cards.visibility = View.VISIBLE
                no_trip_layout.visibility = View.GONE
                progress_bar.showProgressBar()
                this.clearOldData()
                this.viewModel.waypoints = trip.waypoints?.toMutableList()!!
                this.currentTrip = trip
                this.bindUI(trip)
                this.bindAdminUI(trip)
                progress_bar.hideProgressBar()
            })
        })
    }

    private fun clearOldData() {
        this.viewModel.waypoints.clear()
        this.dragableItems.clear()
        this.anotherPlaces.forEach {
           it.second.clear()
            if (::anotherPlacesLayout.isInitialized) {
                this.anotherPlacesLayout.removeView(it.first)

            }
        }
        this.anotherPlacesTetView.forEach {
            if (::anotherPlacesLayout.isInitialized) {
                this.anotherPlacesLayout.removeView(it)

            }
        }
        this.anotherPlaces.clear()
        // this.groupAdapter jest czyszczony przy dodawaniu nowych elementów
    }

    private fun bindAdminUI(trip: Trip) {
        another_places_progress_bar.visibility = View.VISIBLE
        this.initStartAddressPlaces(trip.startAddress!!)
        this.initWaypointsAddressPlaces(trip.waypoints!!)
        this.initEndAddressPlaces(trip.endAddress!!)
    }

    private fun initStartAddressPlaces(
        startAddress: String
    ) = lifecycleScope.launch {
        val nearbyStartAddress =
            this@TripInfoFragment.viewModel.getNearbyPlaces(startAddress).toMutableList()
        var numberOfListedPlace = 0
        val listToAdapter = mutableListOf<String>()
        (0..6).forEachIndexed { index, i ->
            if (nearbyStartAddress.size > index) {
                listToAdapter.add(nearbyStartAddress[index].name)
                numberOfListedPlace++
            }
        }
        val listView = this@TripInfoFragment.createListView()
        val adapter =
            ArrayAdapter<String>(context!!, R.layout.trip_info_list_view_item, listToAdapter)
        val button = this@TripInfoFragment.createButton()
        listView.adapter = adapter
        listView.addFooterView(button)
        listView.onItemClickListener = this@TripInfoFragment.onItemClickListener
        val textView = this@TripInfoFragment.createTextView(
            startAddress
        )
        this@TripInfoFragment.anotherPlacesLayout.addView(
            textView
        )
        this@TripInfoFragment.anotherPlacesTetView.add(textView)
        this@TripInfoFragment.anotherPlaces.add(listView to adapter)
        this@TripInfoFragment.anotherPlacesLayout.addView(listView)

        button.setOnClickListener {
            (1..6).forEach sec@{
                numberOfListedPlace++
                if (nearbyStartAddress.size > numberOfListedPlace) {
                    listToAdapter.add(nearbyStartAddress[numberOfListedPlace].name)
                    adapter.notifyDataSetChanged()
                } else
                    return@sec
            }
        }
    }

    private fun initWaypointsAddressPlaces(
        waypoints: ArrayList<String>
    ) = lifecycleScope.launch {
        waypoints.forEach {
            val nearbyStartAddress =
                this@TripInfoFragment.viewModel.getNearbyPlaces(it).toMutableList()
            var numberOfListedPlace = 0
            val listToAdapter = mutableListOf<String>()
            (0..6).forEachIndexed { index, i ->
                if (nearbyStartAddress.size > index) {
                    listToAdapter.add(nearbyStartAddress[index].name)
                    numberOfListedPlace++
                }
            }
            val listView = this@TripInfoFragment.createListView()
            val adapter =
                ArrayAdapter<String>(context!!, R.layout.trip_info_list_view_item, listToAdapter)
            val button = this@TripInfoFragment.createButton()
            listView.adapter = adapter
            listView.addFooterView(button)
            listView.onItemClickListener = this@TripInfoFragment.onItemClickListener
            val textView = this@TripInfoFragment.createTextView(
                it
            )
            this@TripInfoFragment.anotherPlacesLayout.addView(
                textView
            )
            this@TripInfoFragment.anotherPlacesTetView.add(textView)
            this@TripInfoFragment.anotherPlaces.add(listView to adapter)
            this@TripInfoFragment.anotherPlacesLayout.addView(listView)

            button.setOnClickListener {
                (1..6).forEach sec@{
                    numberOfListedPlace++
                    if (nearbyStartAddress.size > numberOfListedPlace) {
                        listToAdapter.add(nearbyStartAddress[numberOfListedPlace].name)
                        adapter.notifyDataSetChanged()
                    } else
                        return@sec
                }
            }
        }
        another_places_progress_bar.visibility = View.GONE
    }

    private fun initEndAddressPlaces(
        endAddress: String
    ) = lifecycleScope.launch {
        val nearbyStartAddress =
            this@TripInfoFragment.viewModel.getNearbyPlaces(endAddress).toMutableList()
        var numberOfListedPlace = 0
        val listToAdapter = mutableListOf<String>()
        (0..6).forEachIndexed { index, i ->
            if (nearbyStartAddress.size > index) {
                listToAdapter.add(nearbyStartAddress[index].name)
                numberOfListedPlace++
            }
        }
        val listView = this@TripInfoFragment.createListView()
        val adapter =
            ArrayAdapter<String>(context!!, R.layout.trip_info_list_view_item, listToAdapter)
        val button = this@TripInfoFragment.createButton()
        listView.adapter = adapter
        listView.addFooterView(button)
        listView.onItemClickListener = this@TripInfoFragment.onItemClickListener
        val textView = this@TripInfoFragment.createTextView(
            endAddress
        )
        this@TripInfoFragment.anotherPlacesLayout.addView(
            textView
        )
        this@TripInfoFragment.anotherPlacesTetView.add(textView)
        this@TripInfoFragment.anotherPlaces.add(listView to adapter)
        this@TripInfoFragment.anotherPlacesLayout.addView(listView)

        button.setOnClickListener {
            (1..6).forEach sec@{
                numberOfListedPlace++
                if (nearbyStartAddress.size > numberOfListedPlace) {
                    listToAdapter.add(nearbyStartAddress[numberOfListedPlace].name)
                    adapter.notifyDataSetChanged()
                } else
                    return@sec
            }
        }
    }

    /**
     * sprawdza czy nastąpiły zmiany
     * jeżeli tak pokazuje przycisk do zapisania zmian
     */
    private fun checkForUnsavedChanges() {
        if (this.viewModel.waypoints != this.currentTrip.waypoints) {
            this.viewModel.unsavedChanges.postValue(true)
        } else {
            this.viewModel.unsavedChanges.postValue(false)
        }
    }

    private val onItemClickListener =
        AdapterView.OnItemClickListener { parent, _, position, _ ->
            val value = parent.adapter.getItem(position) as String
            if (this.currentUser.idUserFirebase == this.currentTrip.author?.idUserFirebase) {
                this.viewModel.waypoints.add(value)
                this.groupAdapter?.add(DragableItem(value))
                this.groupAdapter?.notifyDataSetChanged()
                this.checkForUnsavedChanges()
            } else {
                Dialog.Builder()
                    .addMessage(getString(R.string.show_route_to) + value)
                    .addPositiveButton("Tak") {
                        lifecycleScope.launch(Dispatchers.Main) {
                            progress_bar.showProgressBar()
                            val latlng = this@TripInfoFragment.viewModel.geocodeAddress(value)
                                .results.first().geometry.location.toLatLng()
                            Intent(context, HomeActivity::class.java).also {
                                val bundle = Bundle()
                                bundle.putString(
                                    ActivitiesAction.HOME_ACTIVITY_DRAW_ROAD.name,
                                    latlng.formatToApi()
                                )
                                it.putExtras(bundle)
                                context?.startActivity(it)
                            }
                            progress_bar.hideProgressBar()
                            it.dismiss()
                        }
                    }
                    .addNegativeButton("Anuluj") {
                        it.dismiss()
                    }
                    .build(parentFragmentManager, javaClass.simpleName)
            }
        }

    private fun createListView(): NonScrollListView {
        val listView = NonScrollListView(context)
        listView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        listView.divider = ContextCompat.getDrawable(context!!, R.color.transparent)
        listView.dividerHeight = 20
        listView.setMargins(context!!, 0, 10, 0, 10)
        return listView
    }

    private fun createButton(): Button {
        val loadMoreButton = Button(context)
        loadMoreButton.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        loadMoreButton.text = getString(R.string.load_more)
        loadMoreButton.setMargins(context!!, 10, 10, 10, 20)
        loadMoreButton.setPadding(0, 5, 0, 5)
        loadMoreButton.setTextColor(Color.WHITE)
        loadMoreButton.textSize = 15F
        loadMoreButton.background = ContextCompat.getDrawable(context!!, R.color.colorPrimary)
        return loadMoreButton
    }

    private fun createTextView(text: String): TextView {
        val textView = TextView(context)
        textView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        textView.setPadding(10, 10, 10, 10)
        textView.text = text
        textView.textSize = 18F
        return textView
    }

    private fun bindUI(trip: Trip) {
        this.tripName.text = trip.name?.trim()
        val date = "${trip.startDate} - ${trip.endDate}".replace("T", " ")
        this.tripTime.text = date
        this.tripStartLocation.text = trip.startAddress?.trim()
        this.tripEndLocation.text = trip.endAddress?.trim()

        val textView = TextView(context)
        textView.setMargins(context!!, 15, 0, 0, 0)
        textView.typeface = Typeface.DEFAULT_BOLD
        textView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)

        if (trip.waypoints != null) {
            this.dragableItems.addAll(this.viewModel.waypoints.toDragableItems())
            if (this.groupAdapter == null || this.tripWaypointsRecyclerView == null) {
                this.groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
                    this.clear()
                    this.addAll(this@TripInfoFragment.dragableItems)
                }
                this.tripWaypointsRecyclerView?.apply {
                    layoutManager = LinearLayoutManager(this@TripInfoFragment.context)
                    adapter = this@TripInfoFragment.groupAdapter
                    this.isNestedScrollingEnabled = false
                }
                this.groupAdapter?.setOnItemClickListener { item, _ ->
                    if (this.viewModel.waypoints.size > 0 && item is DragableItem && this.currentUser.idUserFirebase == this.currentTrip.author?.idUserFirebase) {
                        this.viewModel.waypoints.remove(item.string)
                        this.groupAdapter?.remove(item)
                        this.groupAdapter?.notifyDataSetChanged()
                        this.checkForUnsavedChanges()
                    } else {
                        val value = (item as DragableItem).string
                        Dialog.Builder()
                            .addMessage(getString(R.string.show_route_to) + value)
                            .addPositiveButton("Tak") {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    progress_bar.showProgressBar()
                                    val latlng = this@TripInfoFragment.viewModel.geocodeAddress(value)
                                        .results.first().geometry.location.toLatLng()
                                    Intent(context, HomeActivity::class.java).also {
                                        val bundle = Bundle()
                                        bundle.putString(
                                            ActivitiesAction.HOME_ACTIVITY_DRAW_ROAD.name,
                                            latlng.formatToApi()
                                        )
                                        it.putExtras(bundle)
                                        context?.startActivity(it)
                                    }
                                    progress_bar.hideProgressBar()
                                    it.dismiss()
                                }
                            }
                            .addNegativeButton("Anuluj") {
                                it.dismiss()
                            }
                            .build(parentFragmentManager, javaClass.simpleName)
                    }
                }
                if (this.currentUser.idUserFirebase == this.currentTrip.author?.idUserFirebase) {
                    this.setItemDragToRecyclerView(object : DragItemTouchHelperCallback.OnItemDragListener {
                        override fun onItemDragged(indexFrom: Int, indexTo: Int) {
                            if (indexFrom < indexTo) {
                                for (i in indexFrom until indexTo) {
                                    Collections.swap(this@TripInfoFragment.viewModel.waypoints, i, i + 1)
                                    this@TripInfoFragment.groupAdapter?.notifyItemMoved(i, i + 1)
                                }
                            } else {
                                for (i in indexFrom downTo indexTo + 1) {
                                    Collections.swap(this@TripInfoFragment.viewModel.waypoints, i, i - 1)
                                    this@TripInfoFragment.groupAdapter?.notifyItemMoved(i, i - 1)
                                }
                            }
                        }

                        override fun onDragEnd() {
                            this@TripInfoFragment.checkForUnsavedChanges()
                        }
                    })
                }
            } else {
                this.groupAdapter?.apply {
                    this.clear()
                    this.addAll(this@TripInfoFragment.dragableItems)
                }
            }
        }
    }

    private fun setItemDragToRecyclerView(onItemDrag: DragItemTouchHelperCallback.OnItemDragListener) {
        val dragCallback = DragItemTouchHelperCallback
            .Builder(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
            .setDragEnabled(true)
            .onItemDragListener(onItemDrag)
            .build()
        val itemTouchHelper = ItemTouchHelper(dragCallback)
        itemTouchHelper.attachToRecyclerView(this.tripWaypointsRecyclerView)
    }

    private fun List<String>.toDragableItems(): List<DragableItem> {
        return this.mapIndexed { _, s ->
            DragableItem(s)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.trip_info_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.initViewProperties()
    }

    private fun initViewProperties() {
        this.tripName = trip_name
        this.tripTime = trip_time
        this.tripStartLocation = trip_start_location
        this.tripEndLocation = trip_end_location
        this.tripWaypointsRecyclerView = trip_progress_list_view
        this.anotherPlacesLayout = another_places_layout
    }


}
