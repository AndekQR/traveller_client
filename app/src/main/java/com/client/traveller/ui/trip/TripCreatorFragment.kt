package com.client.traveller.ui.trip

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
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
import com.client.traveller.ui.util.setMargins
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

    private val personForms: MutableList<View> = mutableListOf()
    private val waypointForms: MutableList<View> = mutableListOf()

    private lateinit var tripName: String
    private var tripStartDate: LocalDateTime? = null
    private lateinit var tripStartAddress: String
    private var tripEndDate: LocalDateTime? = null
    private lateinit var tripEndAddress: String
    private var personsEmailString: ArrayList<String> = arrayListOf()
    private var waypointsString: ArrayList<String> = arrayListOf()

    private lateinit var currentUser: User
    private lateinit var currentTrip: Trip

    override val kodein by kodein()
    private val factory: TripViewModelFactory by instance()
    private lateinit var viewModel: TripViewModel

    // zmienna mówi czy właśnie wyświetlamy wycieckę czy torzymy nową
    private var tripView: Boolean = false

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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this, factory).get(TripViewModel::class.java)
        viewModel.getLoggedInUser().observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            this.currentUser = it
        })


        arguments?.let {
            val navigationArguemnts: Trip? = TripCreatorFragmentArgs.fromBundle(it).trip
            // jeżeli jest nullem to tworzymy nową wycieczkę
            if (navigationArguemnts != null) {
                tripView = true
                // blokuje edytowalne pola przed modyfikacją
                this.setEditTextsDisabled()
                // wpisuje w pola dane z wycieczki
                this.insertData(navigationArguemnts)
            } else {
                if (personForms.isEmpty())
                    this.addPerson()
                if (waypointForms.isEmpty())
                    this.addWaypoint()
            }
        }
    }

    private fun insertData(trip: Trip) {
        trip_name.setText(trip.name)
        trip_start_address.setText(trip.startAddress)
        trip_end_address.setText(trip.endAddress)
        trip_start_date.setText(viewModel.formatDateTime(trip.startDate))
        trip_end_date.setText(viewModel.formatDateTime(trip.endDate))
        trip.persons?.forEach { personEmail ->
            this.addPerson(personEmail)
        }
        trip.waypoints?.forEach { waypointAddress ->
            this.addWaypoint(waypointAddress)
        }
        viewModel.getCurrentTrip().observe(viewLifecycleOwner, Observer {
            if (it == null) {
                add_trip_button.setBackgroundColor(Color.GRAY)
                add_trip_button.isEnabled = false
                return@Observer
            } else {
                this.currentTrip = it
                if (this.currentTrip == trip) {
                    add_trip_button.visibility = View.GONE
                } else {
                    if (viewModel.isTripParticipant(trip, currentUser)) {
                        add_trip_button.text = getString(R.string.choose_trip)
                    } else {
                        add_trip_button.text = getString(R.string.send_request_add_to_trip)
                    }
                }
            }
        })

    }

    private fun setEditTextsDisabled() {
        this.setDisabled(trip_name)
        this.setDisabled(trip_start_address)
        this.setDisabled(trip_end_address)
        this.setDisabled(trip_start_date)
        this.setDisabled(trip_end_date)
        personForms.forEach {
            this.setDisabled(it)
        }
        waypointForms.forEach {
            this.setDisabled(it)
        }
    }

    private fun setDisabled(view: View) {
        view.isFocusable = false
        view.isEnabled = false
    }

    private fun setEnabled(view: View) {
        view.isFocusable = true
        view.isEnabled = true
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
            startDate = tripStartDate.toString(),
            startAddress = tripStartAddress,
            endDate = tripEndDate.toString(),
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
            viewModel.addTrip(trip)
        } catch (ex: Exception) {
            com.client.traveller.ui.dialog.Dialog.Builder()
                .addMessage(ex.message!!)
                .addPositiveButton("ok") { dialog ->
                    dialog.dismiss()
                    Navigation.findNavController(this@TripCreatorFragment.view!!)
                        .navigate(R.id.tripListFragment)
                }.build(parentFragmentManager, javaClass.simpleName)
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
        personForms.mapTo(personsEmailString) { it.edit_text.text.toString().trim() }
            .filter { it.isNotBlank() }
        personsEmailString.add(currentUser.email!!) // autor jest też uczestnikiem wycieczki
        waypointForms.mapTo(waypointsString) { it.edit_text.text.toString().trim() }
            .filter { it.isNotBlank() }
    }

    /**
     * Nowa pole gdzie można wpisać dodatkowy punkt na trasie wycieczki
     */
    private fun addWaypoint(name: String? = null) {
        val form = this.addLocationForm()
        form?.let {
            if (name != null) {
                form.edit_text.setText(name)
                this.removeIconForm(it)
                this.setDisabled(it.edit_text)
                it.edit_text.setMargins(context!!, 10, 10, 10, 0)
                it.setMargins(context!!, 0, 10, 0, 0)
            } else {
                form.add_button.setOnClickListener {
                    addWaypoint()
                }
                this.changeToMinusForm(waypointForms)
            }
            waypointForms.add(form)
        }
    }

    /**
     * Nowa pole do wpisana nowej osoby do wycieczki
     */
    private fun addPerson(name: String? = null) {
        val form = this.addPersonForm()
        form?.let {
            // ustawia się gdy chcemy podejrzeć dane wycieczki
            if (name != null) {
                it.edit_text.setText(name)
                this.removeIconForm(it)
                this.setDisabled(it.edit_text)
                it.edit_text.setMargins(context!!, 10, 10, 10, 0)
                it.setMargins(context!!, 0, 10, 0, 0)
            } else {
                // akcja plusa
                form.add_button.setOnClickListener {
                    addPerson()
                }
                // akcja minusa
                this.changeToMinusForm(personForms)
            }
            personForms.add(form)
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

    private fun removeIconForm(form: View) {
        form.add_button.setImageBitmap(null)
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

    /**
     * Akcja po naciśnięciu przycisku "dodaj do wycieczki" w action bar
     */
    // TODO trzeba zrobić powiadomienie do twórcy o tym że ktoś chce dołączyć do wycieczki
    fun jointTripButtonClick() {
        if (this.tripView){

        } else {

        }
    }

}
