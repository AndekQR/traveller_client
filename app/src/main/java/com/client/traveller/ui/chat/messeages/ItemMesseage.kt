package com.client.traveller.ui.chat.messeages

import com.bumptech.glide.Glide
import com.client.traveller.R
import com.client.traveller.data.db.entities.Messeage
import com.client.traveller.data.db.entities.User
import com.client.traveller.ui.util.toLocalDateTime
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.messeage_item_left.*
import kotlinx.android.synthetic.main.messeage_item_right.*

class ItemMesseage(
    private val messeage: Messeage,
    private val currentUser: User,
    private val chatParticipants: MutableSet<User>
) : Item() {

    companion object {
        private const val MSG_TYPE_RIGHT = 0
        private const val MSG_TYPE_LEFT = 1
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            updateAvatar()
            updateText()
            updateSenderName()
            updateSendDate()
        }
    }


    private fun GroupieViewHolder.updateAvatar() {
        val participant = this@ItemMesseage.getUser(messeage.senderIdFirebase)
        when (getMesseageType()) {
            MSG_TYPE_LEFT -> {
                participant?.let {
                    Glide.with(containerView).load(participant.image).into(avatar_left)
                }
            }
            MSG_TYPE_RIGHT -> {
                participant?.let {
                    Glide.with(containerView).load(participant.image).into(avatar_right)
                }
            }
        }
    }

    private fun GroupieViewHolder.updateText() {
        when (getMesseageType()) {
            MSG_TYPE_LEFT -> {
                messeage_item_left_text.text = messeage.messeage
            }
            MSG_TYPE_RIGHT -> {
                messeage_item_right_text.text = messeage.messeage
            }
        }
    }

    private fun GroupieViewHolder.updateSendDate() {
        val date = messeage.sendDate?.toLocalDateTime()
        val hour = when {
            (date?.hour!! < 10) -> "0${date.hour}"
            else -> date.hour.toString()
        }
        val minute = when {
            (date.minute < 10) -> "0${date.minute}"
            else -> date.minute.toString()
        }
        val second = when {
            (date.second < 10) -> "0${date.second}"
            else -> date.second.toString()
        }

        val dateToShow = "$hour:$minute:$second\n" +
                "${date.dayOfMonth}.${date.monthValue}.${date.year}"
        when (getMesseageType()) {
            MSG_TYPE_LEFT -> {
                send_time_left.text = dateToShow
            }
            MSG_TYPE_RIGHT -> {
                send_time_right.text = dateToShow
            }
        }
    }

    private fun GroupieViewHolder.updateSenderName() {
        var sender: User? = null
        chatParticipants.forEach { if (messeage.senderIdFirebase == it.idUserFirebase) sender = it }
        when (getMesseageType()) {
            MSG_TYPE_LEFT -> {
                sender?.let { sender_name_left.text = it.displayName }
            }
            MSG_TYPE_RIGHT -> {
                sender?.let { sender_name_right.text = it.displayName }
            }
        }
    }

    override fun getLayout(): Int {
        return when (this.getMesseageType()) {
            MSG_TYPE_LEFT -> R.layout.messeage_item_left
            else -> R.layout.messeage_item_right
        }
    }

    private fun getUser(idFirebase: String?): User? {
        chatParticipants.forEach { participant ->
            if (participant.idUserFirebase == idFirebase)
                return participant
        }
        return null
    }

    private fun getMesseageType(): Int {
        return if (this.currentUser.idUserFirebase == messeage.senderIdFirebase)
            MSG_TYPE_RIGHT
        else
            MSG_TYPE_LEFT
    }

}



