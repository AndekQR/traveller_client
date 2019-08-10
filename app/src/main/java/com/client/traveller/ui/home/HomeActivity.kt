package com.client.traveller.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.client.traveller.R
import com.client.traveller.ui.auth.LoginActivity
import com.client.traveller.ui.settings.SettingsActivity
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.action_bar.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.nav_header.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, KodeinAware {

    private lateinit var toggle: ActionBarDrawerToggle

    override val kodein by kodein()
    private val factory: HomeViewModelFactory by instance()
    private lateinit var viewModel: HomeViewModel
    private var doublBack = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setStatusBarTranslucent(true)
        setContentView(R.layout.activity_home)

        viewModel = ViewModelProviders.of(this, factory).get(HomeViewModel::class.java)

        floating_search_view.setOnQueryChangeListener { oldQuery, newQuery ->
            //get suggestions based on newQuery

            //pass them on to the search view
            var lista = listOf<MySearchSuggestion>()
            floating_search_view.swapSuggestions(lista)
        }

        floating_search_view.attachNavigationDrawerToMenuButton(drawer_layout)
        navigation_view.setNavigationItemSelectedListener(this)

        viewModel.getLoggedInUser().observe(this, Observer { user ->
            if (user != null){
                setSubtitleNavView(user.email!!)
            }
        })
    }

    private fun setSubtitleNavView(subtitle: String) = GlobalScope.launch(Dispatchers.Main) {
            navigation_view.getHeaderView(0).subtitle.text = subtitle
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.ustawienia_item -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.tworca_item -> {
//                val intent = Intent(this, AuthorActivity::class.java)
//                startActivity(intent)
            }
            R.id.logout -> {
                viewModel.logoutUser()
                Intent(this, LoginActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
            return
        }
       else if (doublBack){
            super.onBackPressed()
            return
        }

        this.doublBack = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doublBack = false }, 2000)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }


}
