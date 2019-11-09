package com.client.traveller.ui.chat.chatList

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.client.traveller.R
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.firestore.model.ChatFirestoreModel
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
import kotlinx.android.synthetic.main.fragment_chat_list.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.util.*
import kotlin.collections.ArrayList

class ChatListFragment : ScopedFragment(), KodeinAware, OnItemClickListener {

    override val kodein by kodein()
    private val factory: ChatViewModelFactory by instance()
    private lateinit var viewModel: ChatViewModel

    private lateinit var currentTrip: Trip
    private lateinit var currentUser: User
    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>

    private var mapChatParticipants = mutableMapOf<ChatFirestoreModel, List<User>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(activity!!, factory).get(ChatViewModel::class.java)
        this.viewModel.currentTrip.observe(viewLifecycleOwner, Observer { trip ->
            if (trip == null) return@Observer
            this.currentTrip = trip
            this.viewModel.currentUser.observe(viewLifecycleOwner, Observer { user ->
                if (user == null) return@Observer
                this.currentUser = user
                this.bindUI()
            })
        })
    }

    private fun bindUI() {
        this.viewModel.initUsersChats(currentUser.idUserFirebase!!, currentTrip.uid!!)
        this.viewModel.currentUserChats.observe(viewLifecycleOwner, Observer { chats ->
            if (chats == null) return@Observer
            progress_bar.showProgressBar()
            launch {
                chats.forEach { chat ->
                    val participants = viewModel.getUsersById(ArrayList(chat.participantsUid?.keys!!))
                    this@ChatListFragment.mapChatParticipants[chat] = participants
                }
                updateChatsList(this@ChatListFragment.mapChatParticipants)
            }

        })
        viewModel.searchQuery.observe(viewLifecycleOwner, Observer { filtr ->
            launch {
                if (filtr.isEmpty()) {
                    this@ChatListFragment.updateChatsList(this@ChatListFragment.mapChatParticipants)
                    return@launch
                } else if (this@ChatListFragment.mapChatParticipants.isNotEmpty()) {
                    progress_bar.showProgressBar()
                    val newChatsList = mutableMapOf<ChatFirestoreModel, List<User>>()
                    this@ChatListFragment.mapChatParticipants.forEach { entry ->
                        val chatParticipants = entry.value
                        chatParticipants.forEach { user ->
                            if (user.displayName?.toLowerCase(Locale.ROOT)?.contains(filtr.toLowerCase(Locale.ROOT))!!) {
                                newChatsList[entry.key] = entry.value
                                return@forEach
                            }
                        }
                    }
                    this@ChatListFragment.updateChatsList(newChatsList)
                }
            }
        })

    }

    private fun updateChatsList(chats: Map<ChatFirestoreModel, List<User>>) {
        progress_bar.hideProgressBar()
        this.groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(chats.toChatItem())
        }
        this.groupAdapter.setOnItemClickListener(this)
        recycler_view?.apply {
            layoutManager = LinearLayoutManager(this@ChatListFragment.context)
            adapter = groupAdapter
        }
    }

    private fun Map<ChatFirestoreModel, List<User>>.toChatItem(): List<ItemChatList> {
        return this.map {
            ItemChatList(it.key, it.value)
        }
    }

    override fun onItemClick(item: Item<*>, view: View) {
        if (item is ItemChatList) {
            Intent(context, MesseageActivity::class.java).also {
                it.putExtra("chatId", item.chat.uid)
                it.putExtra("tripUid", this.currentTrip.uid)
                startActivity(it)
            }
        }
    }

    override fun onStop() {
        super.onStop()

        try {
            this.viewModel.usersChatsRemoveLiveDataObserver(
                currentUser.idUserFirebase!!,
                currentTrip.uid!!
            )
        } catch (ex: NullPointerException) {
            Log.e(javaClass.simpleName, "userChats observer still running")
        }
    }
}
