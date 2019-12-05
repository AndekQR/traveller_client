package com.client.traveller.ui.tripInfo

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.client.traveller.R
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.ui.util.NonScrollListView
import com.client.traveller.ui.util.hideProgressBar
import com.client.traveller.ui.util.setMargins
import com.client.traveller.ui.util.showProgressBar
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.android.synthetic.main.trip_info_fragment.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class TripInfoFragment : Fragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: TripInfoViewModelFactory by instance()
    private lateinit var viewModel: TripInfoViewModel

    private lateinit var tripName: TextView
    private lateinit var tripTime: TextView
    private lateinit var tripStartLocation: TextView
    private lateinit var tripEndLocation: TextView
    private lateinit var tripProgressListView: ListView
    private lateinit var listViewAdapter: ArrayAdapter<String>
    private lateinit var anotherPlacesLayout: LinearLayout

    private lateinit var currentUser: User
    private var waypoints = mutableListOf<String>()
    private var waypointsFormatted = mutableListOf<String>()


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        this.viewModel = ViewModelProvider(activity!!, factory).get(TripInfoViewModel::class.java)

        this.viewModel.currentUser.observe(viewLifecycleOwner, Observer { user ->
            if (user == null) return@Observer
            this.currentUser = user

            this.viewModel.currentTrip.observe(viewLifecycleOwner, Observer { trip ->
                if (trip == null) return@Observer
                this.waypoints = trip.waypoints?.toMutableList()!!
                this.waypointsFormatted = trip.waypoints?.mapIndexed { index, s ->
                    "${index+1}. $s"
                }?.toMutableList()!!
                progress_bar.showProgressBar()
                this.bindUI(trip)
                // to znaczy że autor
//                if (this.currentUser.idUserFirebase == trip.author?.idUserFirebase) {
                    this.bindAdminUI(trip)
//                }
                progress_bar.hideProgressBar()
            })
        })
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
        val nearbyStartAddress = this@TripInfoFragment.viewModel.getNearbyPlaces(startAddress).toMutableList()
        var numberOfListedPlace = 0
        val listToAdapter = mutableListOf<String>()
        (0..6).forEachIndexed { index, i ->
            if (nearbyStartAddress.size > index) {
                listToAdapter.add(nearbyStartAddress[index].name)
                numberOfListedPlace++
            }
        }
        val listView = this@TripInfoFragment.createListView()
        val adapter = ArrayAdapter<String>(context!!, R.layout.trip_info_list_view_item, listToAdapter)
        val button = this@TripInfoFragment.createButton()
        listView.adapter = adapter
        listView.addFooterView(button)
        listView.onItemClickListener = this@TripInfoFragment.onItemClickListener
        this@TripInfoFragment.anotherPlacesLayout.addView(this@TripInfoFragment.createTextView(startAddress))
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
    ) = lifecycleScope.launch  {
        waypoints.forEach {
            val nearbyStartAddress = this@TripInfoFragment.viewModel.getNearbyPlaces(it).toMutableList()
            var numberOfListedPlace = 0
            val listToAdapter = mutableListOf<String>()
            (0..6).forEachIndexed { index, i ->
                if (nearbyStartAddress.size > index) {
                    listToAdapter.add(nearbyStartAddress[index].name)
                    numberOfListedPlace++
                }
            }
            val listView = this@TripInfoFragment.createListView()
            val adapter = ArrayAdapter<String>(context!!, R.layout.trip_info_list_view_item, listToAdapter)
            val button = this@TripInfoFragment.createButton()
            listView.adapter = adapter
            listView.addFooterView(button)
            listView.onItemClickListener = this@TripInfoFragment.onItemClickListener
            this@TripInfoFragment.anotherPlacesLayout.addView(this@TripInfoFragment.createTextView(it))
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
        val nearbyStartAddress = this@TripInfoFragment.viewModel.getNearbyPlaces(endAddress).toMutableList()
        var numberOfListedPlace = 0
        val listToAdapter = mutableListOf<String>()
        (0..6).forEachIndexed { index, i ->
            if (nearbyStartAddress.size > index) {
                listToAdapter.add(nearbyStartAddress[index].name)
                numberOfListedPlace++
            }
        }
        val listView = this@TripInfoFragment.createListView()
        val adapter = ArrayAdapter<String>(context!!, R.layout.trip_info_list_view_item, listToAdapter)
        val button = this@TripInfoFragment.createButton()
        listView.adapter = adapter
        listView.addFooterView(button)
        listView.onItemClickListener = this@TripInfoFragment.onItemClickListener
        this@TripInfoFragment.anotherPlacesLayout.addView(this@TripInfoFragment.createTextView(endAddress))
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

    private val onItemClickListener =
        AdapterView.OnItemClickListener { parent, view, position, id ->
            val value = parent.adapter.getItem(position) as String
            this.waypoints.add(value)
            this.waypointsFormatted.add("${this.waypointsFormatted.size+1}. $value")
            this.listViewAdapter.notifyDataSetChanged()
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
        loadMoreButton.background =  ContextCompat.getDrawable(context!!, R.color.colorPrimary)
        return loadMoreButton
    }

    private fun createTextView(text: String): TextView {
        val textView = TextView(context)
        textView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        textView.setPadding(10,10,10,10)
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

        if (trip.waypoints?.isEmpty()!!) {
            val empty = listOf(getString(R.string.no_trip_waypoints))
            this.listViewAdapter =
                ArrayAdapter(context!!, R.layout.trip_info_list_view_item, empty)
            this.tripProgressListView.adapter = this.listViewAdapter
        } else {
                this.listViewAdapter =
                    ArrayAdapter(context!!, R.layout.trip_info_list_view_item, this.waypointsFormatted)
            this.tripProgressListView.adapter = this.listViewAdapter
            this.tripProgressListView.setOnItemClickListener { parent, view, position, id ->
                if (this.waypoints.size > 0) {
                    this.waypoints.removeAt(position)
                    val _waypointsFormatted = this.waypoints.mapIndexed { index, s ->
                        "${index+1}. $s"
                    }.toMutableList()
                    this.waypointsFormatted.removeAll { true }
                    this.waypointsFormatted.addAll(_waypointsFormatted)
                    this.listViewAdapter.notifyDataSetChanged()
                }
            }
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
        this.tripProgressListView = trip_progress_list_view
        this.anotherPlacesLayout = another_places_layout
    }


}
