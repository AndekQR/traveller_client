package com.client.traveller.ui.chat.messeages

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autofit.et.lib.AutoFitEditText
import com.client.traveller.R
import com.client.traveller.data.db.entities.Messeage
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.firestore.model.ChatFirestoreModel
import com.client.traveller.ui.dialog.Dialog
import com.client.traveller.ui.home.HomeActivity
import com.client.traveller.ui.util.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_messeage.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import org.threeten.bp.LocalDateTime


class MesseageActivity : ScopedAppActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: MesseageViewModelFactory by instance()
    private lateinit var viewModel: MesseageViewModel

    private lateinit var toolBar: Toolbar
    private lateinit var sendButton: ImageButton
    private lateinit var messeageEditText: AutoFitEditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>

    private lateinit var currentUser: User
    private lateinit var currentTrip: Trip

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messeage)

        viewModel = ViewModelProvider(this, factory).get(MesseageViewModel::class.java)
        this.viewModel.currentTrip.observe(this, Observer {
            if (it == null) return@Observer
            this.currentTrip = it

            this.viewModel.currentUser.observeOnce(this, Observer { currentUser ->
                if (currentUser == null) return@Observer
                progress_bar.showProgressBar()
                this.currentUser = currentUser
                launch(Dispatchers.Main) {
                    this@MesseageActivity.viewModel.identifyChat(
                        intent,
                        this@MesseageActivity.currentUser.email
                    )
                    if (this@MesseageActivity.viewModel.chatId == null)
                        viewModel.chatId = viewModel.findChat(
                            viewModel.getChatParticipantsUid(),
                            currentTrip.uid!!
                        )?.uid
                    this@MesseageActivity.setWindowTitle()
                    this@MesseageActivity.tryInitChatMessages()
                    progress_bar.hideProgressBar()
                }
            })
        })


        this.initializeView()


        this.sendButton.setOnClickListener(onSendButtonClick)
    }

    @ExperimentalCoroutinesApi
    private suspend fun tryInitChatMessages() {
        val tripUid = intent.extras!!.getString("tripUid")
        if (this.checkIfCorrectTrip(tripUid)) {
            viewModel.initChatMesseages()
            this@MesseageActivity.viewModel.chatMesseages.observe(
                this@MesseageActivity,
                Observer { messeages ->
                    if (messeages == null) return@Observer
                    if (messeages.isNotEmpty())
                        this@MesseageActivity.updateMesseages(messeages)
                })
        }
    }

    private fun setWindowTitle() {
        when {
            viewModel.chatParticipants.size > 1 -> supportActionBar?.title =
                viewModel.chatParticipants.first().displayName + ", " + viewModel.chatParticipants.elementAt(
                    1
                ).displayName
            viewModel.chatParticipants.size == 1 -> supportActionBar?.title =
                viewModel.chatParticipants.first().displayName
            else -> supportActionBar?.title = "no"
        }
    }

    /**
     * sprawdza czy przekazany w intent uid wycieczki zgadza się z atuaalną wycieczką użytkownika
     * warunek if (tripUid == null) return true jest potrzebny bo tripUid w intent jest przekazywany tylko gdy klikniemy powiadomienie o nowej widomości
     */
    private suspend fun checkIfCorrectTrip(tripUid: String?): Boolean {
        if (tripUid == null) return true
        else if (tripUid == this.currentTrip.uid) return true
        else {
            val messeageTrip = this.viewModel.findTripByUid(tripUid)
            Dialog.Builder()
                .addTitle(getString(R.string.wrong_trip))
                .addMessage(getString(R.string.need_to_join_different_trip) + " \"${messeageTrip.name}\"")
                .addPositiveButton("ok") { dialog ->
                    Intent(this, HomeActivity::class.java).also { intent ->
                        dialog.dismiss()
                        startActivity(intent)
                        this.finish()
                    }
                }
                .build(supportFragmentManager, javaClass.simpleName)
            return false
        }
    }

    private val onSendButtonClick = View.OnClickListener {
        launch {
            if (messeageEditText.text.isEmpty()) return@launch
            val messeage = prepareMesseage()
            messeageEditText.setText("")
            var chat: ChatFirestoreModel? = null
            try {
                chat = viewModel.findChat(
                    participants = viewModel.getChatParticipantsUid(),
                    tripUid = currentTrip.uid!!
                )
            } catch (ex: Exception) {
                Dialog.Builder()
                    .addMessage(getString(R.string.something_went_wrong))
                    .addPositiveButton("ok") {
                        it.dismiss()
                    }
                    .build(supportFragmentManager, javaClass.simpleName)
            }
            chat?.let { viewModel.sendMesseage(it, messeage) }
        }
    }

    private fun prepareMesseage(): Messeage {
        val messeageText = messeageEditText.text.toString().trim()
        return Messeage(
            senderIdFirebase = currentUser.idUserFirebase,
            messeage = messeageText,
            sendDate = LocalDateTime.now().toLong(),
            uid = randomUid()
        )
    }

    private fun initializeView() {
        // inicjalizacja actionbara
        this.toolBar = toolbar
        this.setSupportActionBar(this.toolBar)
        this.supportActionBar?.setHomeButtonEnabled(true)
        this.supportActionBar?.setDisplayShowHomeEnabled(true)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        this.recyclerView = recycler_view
        this.messeageEditText = messeage
        this.sendButton = send_button
    }

    private val recyclerViewOnLayoutChange =
        View.OnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                if (this.recyclerView.adapter?.itemCount!! > 0)
                    this.recyclerView.postDelayed(
                        { this.recyclerView.smoothScrollToPosition(this.recyclerView.adapter?.itemCount!! - 1) },
                        100
                    )
            }
        }

    private fun updateMesseages(messeages: List<Messeage>) {
        this.groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(messeages.toMesseageItem())
        }
        this.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MesseageActivity)
            adapter = groupAdapter
            addOnLayoutChangeListener(recyclerViewOnLayoutChange)
            if (messeages.isNotEmpty())
                smoothScrollToPosition(messeages.size - 1)
        }
    }

    private fun List<Messeage>.toMesseageItem(): List<ItemMesseage> {
        return this.map {
            ItemMesseage(it, currentUser, viewModel.chatParticipants)
        }
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
