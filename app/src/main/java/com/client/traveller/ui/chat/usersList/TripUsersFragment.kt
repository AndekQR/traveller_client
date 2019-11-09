package com.client.traveller.ui.chat.usersList

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.client.traveller.R
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.ui.chat.ChatViewModel
import com.client.traveller.ui.chat.ChatViewModelFactory
import com.client.traveller.ui.chat.messeages.MesseageActivity
import com.client.traveller.ui.util.ScopedFragment
import com.client.traveller.ui.util.hideProgressBar
import com.client.traveller.ui.util.showProgressBar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_trip_users.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.util.*

class TripUsersFragment : ScopedFragment(), KodeinAware, OnItemClickListener {

    override val kodein by kodein()
    private val factory: ChatViewModelFactory by instance()
    private lateinit var viewModel: ChatViewModel

    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var currentTrip: Trip
    private var allParticipants = listOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trip_users, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(activity!!, factory).get(ChatViewModel::class.java)
        this.bindUI()
    }

    private fun bindUI() {
        viewModel.currentTrip.observe(viewLifecycleOwner, Observer { trip ->
            if (trip == null) return@Observer
            progress_bar.showProgressBar()
            this.currentTrip = trip
            launch(Dispatchers.Main) {
                val users = viewModel.getUsersByEmails(trip.persons)
                users?.let {
                    this@TripUsersFragment.allParticipants = it
                    updateUsersList(it)
                    progress_bar.hideProgressBar()
                }
            }
        })
        this.viewModel.searchQuery.observe(viewLifecycleOwner, Observer {filtr ->
            launch {
                progress_bar.showProgressBar()
                if (filtr.isEmpty() && this@TripUsersFragment.allParticipants.isNotEmpty()){
                    this@TripUsersFragment.updateUsersList(this@TripUsersFragment.allParticipants)
                    progress_bar.hideProgressBar()
                    return@launch
                } else if (this@TripUsersFragment.allParticipants.isNotEmpty()) {
                    val newParticipantsList = mutableListOf<User>()
                    this@TripUsersFragment.allParticipants.forEach { participant ->
                        val result = participant.displayName?.toLowerCase(Locale.ROOT)?.contains(filtr.toLowerCase(Locale.ROOT))
                        if (result != null && result) newParticipantsList.add(participant)
                    }
                    this@TripUsersFragment.updateUsersList(newParticipantsList)
                    progress_bar.hideProgressBar()
                }
            }
        })
    }

    private fun updateUsersList(users: List<User>) {
        groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(users.toUserItem())
        }
        groupAdapter.setOnItemClickListener(this)
        recycler_view?.apply {
            layoutManager = LinearLayoutManager(this@TripUsersFragment.context)
            adapter = groupAdapter
        }
    }

    private fun List<User>.toUserItem(): List<ItemUsersListChat> {
        return this.map {
            ItemUsersListChat(it, context!!)
        }
    }

    override fun onItemClick(item: Item<*>, view: View) {
        if (item is ItemUsersListChat) {
            Intent(context, MesseageActivity::class.java).also {
                it.putExtra("userId", item.user.idUserFirebase)
                it.putExtra("tripUid", this.currentTrip.uid)
                startActivity(it)
            }
        }
    }
}
