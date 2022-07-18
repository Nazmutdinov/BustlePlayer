package com.example.bustleplayer

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemTouchHelperCallback @Inject constructor (
    private val callbackSwipeLeft: (Int) -> Unit,
    private val callbackMoveItem: (Int, Int) -> Unit,
    private val callbackSaveOrdering: () -> Unit
    ) :
    ItemTouchHelper.SimpleCallback(DOWN or UP , ItemTouchHelper.LEFT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val adapter = recyclerView.adapter
        val oldPosition = viewHolder.absoluteAdapterPosition
        val newPosition = target.absoluteAdapterPosition

        adapter?.notifyItemMoved(oldPosition, newPosition)

        callbackMoveItem(oldPosition,newPosition)

        return true
    }

    /**
     * сохраним все перестановки в плейлист в БД
     * это из-за того, что нет события на drop
     */
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        callbackSaveOrdering()
    }

    override fun isLongPressDragEnabled(): Boolean  = true

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.absoluteAdapterPosition

        if (direction == ItemTouchHelper.LEFT) callbackSwipeLeft(position)
    }

    override fun isItemViewSwipeEnabled(): Boolean = true
}