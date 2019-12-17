package com.client.traveller.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.client.traveller.R
import com.client.traveller.ui.auth.AuthActivity
import com.client.traveller.ui.home.HomeActivity
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

/**
 * ekran z logiem aplikacji przy uruchamimaniu apllikacji
 * eliminuje skakanie po ekranach
 */
class SplashActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: SplashViewModelFactory by instance()
    private lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_StatusBar_Transparent)
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, factory).get(SplashViewModel::class.java)

        lifecycleScope.launch {
            val currentUser = this@SplashActivity.viewModel.getCurrentUser()
            if (currentUser != null) {
                Intent(this@SplashActivity, HomeActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                    this@SplashActivity.finish()
                }
            } else {
                Intent(this@SplashActivity, AuthActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                    this@SplashActivity.finish()
                }
            }
        }
    }
}