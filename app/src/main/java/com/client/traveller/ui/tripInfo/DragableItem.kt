package com.client.traveller.ui.tripInfo

import com.client.traveller.R
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.trip_info_recyclerview_dragable_item.*

class DragableItem(
    val string: String
): Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.text.text = string
    }

    override fun getLayout() = R.layout.trip_info_recyclerview_dragable_item

}