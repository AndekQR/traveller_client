package com.client.traveller.ui.chat.chatList

import com.client.traveller.data.db.entities.Messeage
import com.client.traveller.data.db.entities.User
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item

class ItemChtatList(
    private val messeage: Messeage,
    private val currentUser: User
) : Item() {


    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLayout(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}