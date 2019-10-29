package com.client.traveller.ui.chat.messeage

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.autofit.et.lib.AutoFitEditText
import com.client.traveller.R
import com.client.traveller.ui.util.ScopedAppActivity
import kotlinx.android.synthetic.main.activity_messeage.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class MesseageActivity : ScopedAppActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: MesseageViewModelFactory by instance()
    private lateinit var viewModel: MesseageViewModel

    private lateinit var toolBar: Toolbar
    private lateinit var sendButton: ImageButton
    private lateinit var messeageEditText: AutoFitEditText
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messeage)

        viewModel = ViewModelProvider(this, factory).get(MesseageViewModel::class.java)

        // inicjalizacja actionbara
        this.toolBar = toolbar
        this.setSupportActionBar(this.toolBar)
        this.supportActionBar?.setHomeButtonEnabled(true)
        this.supportActionBar?.setDisplayShowHomeEnabled(true)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        launch(Dispatchers.Main) {
            viewModel.setIdentifier(intent)
            if (viewModel.chatParticipants.size > 1){
                title = viewModel.chatParticipants.first().displayName + ", ..."
            } else if (viewModel.chatParticipants.size == 1) {
                supportActionBar?.title = viewModel.chatParticipants.first().displayName
            }
        }

        this.recyclerView = recycler_view
        this.messeageEditText = messeage
        this.sendButton = send_button
    }

    private fun clearData() {
        viewModel.userId = null
        viewModel.chatId = null
        viewModel.chatParticipants.removeAll { true }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.clearData()
    }

}
