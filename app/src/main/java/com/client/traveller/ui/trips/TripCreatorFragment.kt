package com.client.traveller.ui.trips

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.client.traveller.R
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.ui.util.ScopedFragment
import com.client.traveller.ui.util.showProgressBar
import kotlinx.android.synthetic.main.fragment_trip_creator.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.android.synthetic.main.trip_creator_edit_form.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class TripCreatorFragment : ScopedFragment(), KodeinAware {

    private val persons: MutableList<View> = mutableListOf()
    private val waypoints: MutableList<View> = mutableListOf()

    private lateinit var tripName: String
    private var tripStartDate: LocalDateTime? = null
    private lateinit var tripStartAddress: String
    private var tripEndDate: LocalDateTime? = null
    private lateinit var tripEndAddress: String
    private var personsEmailString: List<String> = listOf()
    private var waypointsString: List<String> = listOf()

    private lateinit var currentUser: User

    override val kodein by kodein()
    private val factory: TripViewModelFactory by instance()
    private lateinit var viewModel: TripViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trip_creator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        add_trip_button.setOnClickListener {
            progress_bar.showProgressBar()
            this.newTrip()
        }

        trip_start_date.setOnClickListener {
            this.pickDate(trip_start_date)
        }

        trip_end_date.setOnClickListener {
            this.pickDate(trip_end_date)
        }


        if (persons.isEmpty())
            this.addPerson()
        if (waypoints.isEmpty())
            this.addWaypoint()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this, factory).get(TripViewModel::class.java)

        viewModel.getLoggedInUser().observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            currentUser = it
        })

    }

    /**
     * Akcja po naciśnięciu przyciksu dodającego nową wycieczkę
     * - ładuje dane z edittextów
     * - sprawdza załadowane dane
     * - dodaje wycieczke do lokalnej bazy danych i do firesotre
     * - wyświetla wiadomość w przypadku błędu
     *
     * Asynchronicznie
     */
    private fun newTrip() = launch(Dispatchers.Main) {
        this@TripCreatorFragment.loadData()
        val result = this@TripCreatorFragment.validate()
        if (!result) {
            Toast.makeText(
                requireContext(),
                getString(R.string.something_went_wrong),
                Toast.LENGTH_SHORT
            ).show()
            return@launch
        }

        val trip = Trip(
            name = tripName,
            author = currentUser,
            start = tripStartDate.toString(),
            startAddress = tripStartAddress,
            end = tripEndDate.toString(),
            endAddress = tripEndAddress,
            persons = personsEmailString,
            waypoints = waypointsString
        )
        this@TripCreatorFragment.addTripShowResult(trip)
    }

    /**
     * Metoda suspend wykonuje wszyzstko po koleji
     * Suspend bo musimy czekać na wynik dodawania wycieczki do baz danych
     */
    private suspend fun addTripShowResult(trip: Trip) {
        try {
            viewModel.addTripAsync(trip)
        } catch (ex: Exception) {
            com.client.traveller.ui.dialog.Dialog.Builder()
                .addMessage(ex.message!!)
                .addPositiveButton("ok") { dialog ->
                    dialog.dismiss()
                    Navigation.findNavController(this@TripCreatorFragment.view!!)
                        .navigate(R.id.tripListFragment)
                }.build(fragmentManager, javaClass.simpleName)
            return
        }
        Navigation.findNavController(this@TripCreatorFragment.view!!)
            .navigate(R.id.tripListFragment)
    }

    /**
     * Metoda ładuje dane z pól do edycji
     * Usuwa też spacje na początku i na końcu
     */
    private fun loadData() {
        tripName = trip_name.text.toString().trim()
        tripStartAddress = trip_start_address.text.toString().trim()
        tripEndAddress = trip_end_address.text.toString().trim()
        personsEmailString = persons.map {
            it.edit_text.text.toString().trim()
        }
        personsEmailString = personsEmailString.filter { email: String ->
            email.isNotBlank()
        }
        waypointsString = waypoints.map {
            it.edit_text.text.toString().trim()
        }
        waypointsString = waypointsString.filter {
            it.isNotBlank()
        }
    }

    /**
     * Nowa pole gdzie można wpisać dodatkowy punkt na trasie wycieczki
     */
    private fun addWaypoint() {
        val form = this.addLocationForm()
        form?.let {
            form.add_button.setOnClickListener {
                addWaypoint()
            }
            this.changeToMinusForm(waypoints)
            waypoints.add(form)
        }
    }

    /**
     * Nowa pole do wpisana nowej osoby do wycieczki
     */
    private fun addPerson() {
        val form = this.addPersonForm()
        form?.let {
            // akcja plusa
            form.add_button.setOnClickListener {
                addPerson()
            }
            // akcja minusa
            this.changeToMinusForm(persons)
            persons.add(form)
        }
    }

    private fun addPersonForm(): View? {
        val personFormLayout = R.layout.trip_creator_edit_form
        val formView = LayoutInflater.from(this.context).inflate(personFormLayout, null, false)
        formView.edit_text.setHint(R.string.person_hint)
        persons_list_relative_layout.addView(formView)
        return formView
    }

    private fun addLocationForm(): View? {
        val tripFormLayout = R.layout.trip_creator_edit_form
        val formView = LayoutInflater.from(this.context).inflate(tripFormLayout, null, false)
        formView.edit_text.setHint(R.string.waypoint_hint)
        waypoints_list_relative_layout.addView(formView)
        return formView
    }

    /**
     * Zmiana ikony przycisku dodawania na minusa i zmiana jego acji na usuwanie danego pola do edycji
     */
    private fun changeToMinusForm(list: MutableList<View>) {
        if (list.isNotEmpty()) {
            list.last().apply {
                add_button.setImageResource(R.drawable.ic_remove)
                add_button.setOnClickListener {
                    this.visibility = View.GONE
                    list.remove(this)
                }
            }
        }
    }

    /**
     * Wyświetla dialog z kalndarzem, po wybraniu daty wyśietla dialog do wybrania godziny
     */
    private fun pickDate(form: EditText) {
        var date = LocalDate.now()
        var time = LocalTime.now()
        context?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                DatePickerDialog(
                    it,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        TimePickerDialog(
                            it,
                            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                                date = LocalDate.of(year, month, dayOfMonth)
                                time = LocalTime.of(hourOfDay, minute)

                                if (form.id == trip_start_date.id)
                                    tripStartDate = LocalDateTime.of(date, time)
                                if (form.id == trip_end_date.id)
                                    tripEndDate = LocalDateTime.of(date, time)

                                form.setText("$date, $time")
                            },
                            time.hour,
                            time.minute,
                            true
                        ).show()
                    },
                    date.year,
                    date.monthValue,
                    date.dayOfMonth
                ).show()
            } else {
                TODO("VERSION.SDK_INT < N")
            }
        }
    }

    /**
     * Walidacja danych
     */
    private fun validate(): Boolean {

        if (tripEndDate == null || tripStartDate == null || tripEndAddress.isEmpty() || tripStartAddress.isEmpty()
            || tripName.isEmpty()
        )
            return false

        if (personsEmailString.isNotEmpty()) {
            personsEmailString.forEach {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches())
                    return false
            }
        }

        if (tripStartDate?.isBefore(LocalDateTime.now())!!)
            return false

        if (tripStartDate?.isAfter(tripEndDate)!! || tripEndDate?.isBefore(tripStartDate)!!)
            return false

        return true
    }

}
