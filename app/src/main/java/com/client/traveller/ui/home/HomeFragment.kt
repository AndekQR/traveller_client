package com.client.traveller.ui.home


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.R
import com.client.traveller.ui.auth.AuthActivity
import com.client.traveller.ui.settings.SettingsActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.action_bar.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.nav_header.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class HomeFragment : Fragment(), KodeinAware, NavigationView.OnNavigationItemSelectedListener {

    override val kodein by kodein()
    private val factory: HomeViewModelFactory by instance()
    private lateinit var viewModel: HomeViewModel
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // do obługi kliknięć w menu bocznym
        this.setHasOptionsMenu(true)

        viewModel = activity?.run {
            ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        } ?: throw Exception("Invalid activity")

        viewModel.getLoggedInUser().observe(this, Observer { user ->
            if (user != null) {
                setSubtitleNavView(user.email!!)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    /**
     * [onViewCreated] jest wywoływana po [onCreateView] dlatego tutaj wykouje operację na składnikach widoku
     * można też po metodzie inflate w [onCreateView]
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        floating_search_view.setOnQueryChangeListener { oldQuery, newQuery ->
            //get suggestions based on newQuery
            //https://github.com/arimorty/floatingsearchview/blob/master/sample/src/main/java/com/arlib/floatingsearchviewdemo/data/DataHelper.java
            //pass them on to the search view
            val lista = listOf<MySearchSuggestion>()
            floating_search_view.swapSuggestions(lista)
        }

        floating_search_view.attachNavigationDrawerToMenuButton(drawer_layout)
        navigation_view.setNavigationItemSelectedListener(this)

        // childFragmentManager służy do zarządzania fragmentami w tym fagmencie
        // a fragmentManager do zarządzania fragmentami które są związane z activity tego fragmentu
        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)?.let {
            viewModel.initLocationProvider(it, activity!!, savedInstanceState)
        }
    }


    private fun setSubtitleNavView(subtitle: String) = GlobalScope.launch(Dispatchers.Main) {
        navigation_view.getHeaderView(0).subtitle.text = subtitle
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.ustawienia_item -> {
                val intent = Intent(activity, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.tworca_item -> {
//                val intent = Intent(this, AuthorActivity::class.java)
//                startActivity(intent)
            }
            R.id.logout -> {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                val mGoogleSignInClient = GoogleSignIn.getClient(activity!!, gso)

                viewModel.logoutUser(mGoogleSignInClient)
                Intent(activity, AuthActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }


}
