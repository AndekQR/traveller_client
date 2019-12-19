package com.client.traveller.ui.chat.chatList

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.client.traveller.R
import com.client.traveller.data.db.entities.Messeage
import com.client.traveller.data.db.entities.Trip
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.firestore.model.ChatFirestoreModel
import com.client.traveller.ui.chat.ChatViewModel
import com.client.traveller.ui.chat.ChatViewModelFactory
import com.client.traveller.ui.chat.messeages.MesseageActivity
import com.client.traveller.ui.util.ScopedFragment
import com.client.traveller.ui.util.hideProgressBar
import com.client.traveller.ui.util.observeOnce
import com.client.traveller.ui.util.showProgressBar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_chat_list.*
import kotlinx.android.synthetic.main.progress_bar.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private lateinit var chatsLastMesseage: Map<String, Messeage>
    private var listOfItems = listOf<ItemChatList>()

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
        this.viewModel.currentUser.observeOnce(viewLifecycleOwner, Observer { user ->
            if (user == null) return@Observer
            this.currentUser = user

            this.viewModel.currentTrip.observe(viewLifecycleOwner, Observer { trip ->
                if (trip == null) return@Observer
                if (!::currentTrip.isInitialized || this.currentTrip != trip) {
                    this.currentTrip = trip
                    this.bindUI()
                }
            })
        })

    }

    private fun bindUI() {
        progress_bar.showProgressBar()
        val mutext = Mutex()
        this.viewModel.initUsersChats(currentUser.idUserFirebase!!, currentTrip.uid!!)
        this.viewModel.currentUserChats.observe(viewLifecycleOwner, Observer { chats ->
            if (chats == null) return@Observer
            if (this.mapChatParticipants.keys.toList() == chats && chats.isNotEmpty()) return@Observer
            if (chats.isEmpty()) {
                progress_bar.hideProgressBar()
                return@Observer
            }
            progress_bar.showProgressBar()
            this.initChatsLastMessage(chats)
            this.mapChatParticipants.clear()
            this.mapChatParticipants.putAll(chats.map { it to listOf<User>() }.toMap())
            launch {
                mutext.withLock {
                    this@ChatListFragment.mapChatParticipants.clear()
                    chats.forEach { chat ->
                        val participants =
                            viewModel.getUsersById(ArrayList(chat.participantsUid?.keys!!))
                        this@ChatListFragment.mapChatParticipants[chat] = participants
                    }
                    this@ChatListFragment.updateChatsList(this@ChatListFragment.mapChatParticipants)
                    progress_bar.hideProgressBar()
                }
            }

        })
        viewModel.searchQuery.observe(viewLifecycleOwner, Observer { filtr ->
            launch {
                progress_bar.showProgressBar()
                this@ChatListFragment.filterChats(filtr)
                    ?.let { this@ChatListFragment.updateChatsList(it) }
                progress_bar.hideProgressBar()
            }
        })

    }

    private fun filterChats(filtr: String): MutableMap<ChatFirestoreModel, List<User>>? {
        if (filtr.isEmpty()) {
            this@ChatListFragment.updateChatsList(this@ChatListFragment.mapChatParticipants)
            return null
        } else if (this@ChatListFragment.mapChatParticipants.isNotEmpty()) {
            val newChatsList = mutableMapOf<ChatFirestoreModel, List<User>>()
            this@ChatListFragment.mapChatParticipants.forEach { entry ->
                val chatParticipants = entry.value
                chatParticipants.forEach { user ->
                    if (user.displayName?.toLowerCase(Locale.ROOT)?.contains(
                            filtr.toLowerCase(
                                Locale.ROOT
                            )
                        )!!
                    ) {
                        newChatsList[entry.key] = entry.value
                        return@forEach
                    }
                }
            }
            return newChatsList
        }
        return null
    }

    private fun initChatsLastMessage(chats: List<ChatFirestoreModel>) {
        viewModel.initChatsLastMessage(chats.map { it.uid!! })
        viewModel.chatsLastMessage.observe(viewLifecycleOwner, Observer { t ->
            if (t == null) return@Observer
            if (::chatsLastMesseage.isInitialized && t.values.toList() == this.chatsLastMesseage.values.toList()) return@Observer
            this.chatsLastMesseage = t.toMap()
            this@ChatListFragment.updateChatsList(this@ChatListFragment.mapChatParticipants)

        })
    }

    private fun updateChatsList(chats: Map<ChatFirestoreModel, List<User>>) {
        this.groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            clear()
            val items = chats.toChatItem()
            this@ChatListFragment.listOfItems = items
            addAll(items)
        }
        this.groupAdapter.setOnItemClickListener(this)
        recycler_view?.apply {
            layoutManager = LinearLayoutManager(this@ChatListFragment.context)
            adapter = groupAdapter
        }
    }

    private fun Map<ChatFirestoreModel, List<User>>.toChatItem(): List<ItemChatList> {
        return this.map {
            val myLastMessage = this@ChatListFragment.chatsLastMesseage[it.key.uid]
            ItemChatList(it.key, it.value, myLastMessage)
        }
    }

    override fun onItemClick(item: Item<*>, view: View) {
        if (item is ItemChatList) {
            Intent(context, MesseageActivity::class.java).also {
                it.putExtra("chatId", item.chat.uid)
                startActivity(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        this.updateChatsList(this.mapChatParticipants)
    }
}
