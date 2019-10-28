package com.client.traveller.ui.chat.usersList

import android.content.Context
import com.bumptech.glide.Glide
import com.client.traveller.R
import com.client.traveller.data.db.entities.User
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_users_list_chat.*

class ItemUsersListChat(
    val user: User,
    val context: Context
) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            updateUserImage()
            updateUserName()
        }
    }

    override fun getLayout() = R.layout.item_users_list_chat

    private fun GroupieViewHolder.updateUserImage() {
        Glide.with(context).load(user.image).into(image_user)
    }

    private fun GroupieViewHolder.updateUserName() {
        user_name.text = user.displayName
    }
}