package com.client.traveller.ui.tripInfo

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class DragItemTouchHelperCallback(
    private val builder: Builder
) : ItemTouchHelper.SimpleCallback(builder.dragDirs, builder.swipeDirs) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        if (viewHolder.itemViewType != target.itemViewType) {
            return false
        }
        this.builder.onItemDragListener.onItemDragged(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }


    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        this.builder.onItemDragListener.onDragEnd()
        super.clearView(recyclerView, viewHolder)
    }


    interface OnItemDragListener {
        fun onItemDragged(indexFrom: Int, indexTo: Int)
        fun onDragEnd()
    }

    override fun isLongPressDragEnabled(): Boolean {
        return builder.dragEnabled
    }

     class Builder(
         val dragDirs: Int,
         val swipeDirs: Int
    ) {

        lateinit var onItemDragListener: OnItemDragListener
        var dragEnabled: Boolean = false

        fun onItemDragListener(onItemDragListener: OnItemDragListener): Builder {
            this.onItemDragListener = onItemDragListener
            return this
        }

        fun setDragEnabled(value: Boolean): Builder {
            this.dragEnabled = value
            return this
        }

        fun build(): DragItemTouchHelperCallback {
            return DragItemTouchHelperCallback(this)
        }
    }
}