package com.client.traveller.ui.chat.messeage

import com.client.traveller.R
import com.client.traveller.data.db.entities.Messeage
import com.client.traveller.data.db.entities.User
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item

class ItemMesseage(
    private val messeage: Messeage,
    private val currentUser: User
) : Item() {

    companion object {
        private const val MSG_TYPE_RIGHT = 0
        private const val MSG_TYPE_LEFT = 1
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLayout(): Int {
        return when(this.getMesseageType()){
            MSG_TYPE_LEFT -> R.layout.messeage_item_left
            else -> R.layout.messeage_item_right
        }
    }

    private fun getMesseageType(): Int {
        return if (this.currentUser.idUserFirebase == messeage.senderIdFirebase)
            MSG_TYPE_RIGHT
        else
            MSG_TYPE_LEFT
    }

}