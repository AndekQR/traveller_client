package com.client.traveller.ui.chat.usersList

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
import com.client.traveller.ui.util.ScopedFragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_trip_users.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class TripUsersFragment : ScopedFragment(), KodeinAware, OnItemClickListener {

    override val kodein by kodein()
    private val factory: ChatViewModelFactory by instance()
    private lateinit var viewModel: ChatViewModel

    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var currentTrip: Trip

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trip_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pull_to_refresh_layout.setOnRefreshListener {
            launch(Dispatchers.Main) {
                viewModel.refreshUsers(currentTrip.persons)
                pull_to_refresh_layout.isRefreshing = false
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(activity!!, factory).get(ChatViewModel::class.java)
        this.bindUI()
    }

    private fun bindUI() {

        viewModel.currentTrip.observe(viewLifecycleOwner, Observer { trip ->
            if (trip == null) return@Observer
            this.currentTrip = trip
            launch(Dispatchers.Main) {
                viewModel.refreshUsers(trip.persons)
                viewModel.usersTrip.observe(viewLifecycleOwner, Observer {
                    if (it == null) return@Observer
                    updateUsersList(it)
                })
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

        }
    }
}
