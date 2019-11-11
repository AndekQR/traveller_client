package com.client.traveller.ui.chat.chatList

import android.graphics.BitmapFactory
import com.client.traveller.R
import com.client.traveller.data.db.entities.User
import com.client.traveller.data.network.firebase.firestore.model.ChatFirestoreModel
import com.client.traveller.ui.util.Coroutines.main
import com.stfalcon.multiimageview.MultiImageView
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_chat_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

class ItemChatList(
    val chat: ChatFirestoreModel,
    private val users: List<User>
) : Item() {


    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            updateAvatar()
            updateText()
        }
    }

    private fun GroupieViewHolder.updateAvatar() = main {
        val multiImageView = multi_image_view as MultiImageView
        multiImageView.clear()
        multiImageView.shape = MultiImageView.Shape.CIRCLE
        val participants = users
        participants.forEach { user ->
            val bitmap = getBitmapFromUrl(user.image!!)
            bitmap?.let { multiImageView.addImage(it) }
        }
    }

    private fun GroupieViewHolder.updateText() = main {
        val participants = users
        val text: StringBuilder = StringBuilder()
        for (i in participants.indices) {
            if (i == participants.lastIndex)
                text.append(participants[i].displayName)
            else
                text.append(participants[i].displayName + ", ")
        }
        nameChat.text = text
    }

    override fun getLayout() = R.layout.item_chat_list

    private suspend fun getBitmapFromUrl(urlString: String) = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString)
            val connection = url.openConnection().apply {
                doInput = true
                connect()
            }
            val stream = connection.getInputStream()
            BitmapFactory.decodeStream(stream)
        } catch (ex: IOException) {
            null
        }
    }

}