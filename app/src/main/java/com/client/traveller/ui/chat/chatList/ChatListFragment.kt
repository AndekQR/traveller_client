package com.client.traveller.ui.chat.chatList

import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.client.traveller.R
import com.client.traveller.data.db.entities.Messeage
import com.client.traveller.ui.chat.ChatViewModel
import com.client.traveller.ui.chat.ChatViewModelFactory
import com.client.traveller.ui.util.ScopedFragment
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class ChatListFragment : ScopedFragment(), KodeinAware{

    override val kodein by kodein()
    private val factory: ChatViewModelFactory by instance()
    private lateinit var viewModel: ChatViewModel

    private lateinit var currentUserMesseages: List<Messeage>

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
        this.viewModel.currentUserMesseages.observe(viewLifecycleOwner, Observer {messeages: List<Messeage>? ->
            if (messeages == null) return@Observer
            this.currentUserMesseages = messeages
        })
    }
}
