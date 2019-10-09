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
import com.client.traveller.ui.util.ScopedFragment
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.nav_header.view.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class HomeFragment : ScopedFragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: HomeViewModelFactory by instance()
    private lateinit var viewModel: HomeViewModel
    private lateinit var toggle: ActionBarDrawerToggle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // do obługi kliknięć w menu bocznym
        this.setHasOptionsMenu(true)
    }

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

        viewModel.getLoggedInUser().observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                setSubtitleNavView(user.email!!)
            }
        })

        // childFragmentManager służy do zarządzania fragmentami w tym fagmencie
        // a fragmentManager do zarządzania fragmentami które są związane z activity tego fragmentu
        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)?.let {
            activity?.let { activity ->
                viewModel.initLocationProvider(it, activity, savedInstanceState)
            }
        }


    }

    private fun setSubtitleNavView(subtitle: String) = launch {
//        navigation_view.getHeaderView(0).subtitle.text = subtitle
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }


}
