package com.eternal_search.recyclerviewadapter

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class SimpleRecyclerViewAdapter<T: RecyclerViewItem>(
		context: Context
): RecyclerViewAdapter<T>(context) {
	var items = mutableListOf<T>()
	private var itemTouchHelper: ItemTouchHelper? = null
	
	fun enableItemTouchHelper(isLongPressDragEnabled: Boolean, isItemSwipeEnabled: Boolean) {
		if (itemTouchHelper != null) {
			throw IllegalStateException("Item touch helper already enabled")
		}
		if (recyclerView != null) {
			throw IllegalStateException("enableItemTouchHelper() must be called before RecyclerView attach")
		}
		itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(
				isLongPressDragEnabled, isItemSwipeEnabled
		))
	}
	
	override fun saveState(bundle: Bundle) {
		super.saveState(bundle)
		bundle.putParcelableArrayList("items", items.toMutableList() as ArrayList<T>)
	}
	
	override fun restoreState(bundle: Bundle) {
		super.restoreState(bundle)
		items.clear()
		items.addAll(bundle.getParcelableArrayList<T>("items")!!)
	}
	
	override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
		super.onAttachedToRecyclerView(recyclerView)
		itemTouchHelper?.attachToRecyclerView(recyclerView)
	}
	
	fun startDrag(viewHolder: RecyclerViewHolder<*>) {
		itemTouchHelper?.startDrag(viewHolder)
	}
	
	fun startSwipe(viewHolder: RecyclerViewHolder<*>) {
		itemTouchHelper?.startSwipe(viewHolder)
	}
	
	override fun getItem(index: Int): T = items[index]
	
	override fun getItemCount(): Int = items.size
	
	inner class ItemTouchHelperCallback(
			private val longPressDragEnabled: Boolean,
			private val swipeEnabled: Boolean
	): ItemTouchHelper.Callback() {
		private var itemMoved = false
		
		override fun isLongPressDragEnabled(): Boolean = longPressDragEnabled
		
		override fun isItemViewSwipeEnabled(): Boolean = swipeEnabled
		
		override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
			val vh = viewHolder as RecyclerViewHolder<*>
			return makeMovementFlags(
					if (vh.canMove())
						if (recyclerView.layoutManager is LinearLayoutManager)
							ItemTouchHelper.UP or ItemTouchHelper.DOWN
						else
							ItemTouchHelper.UP or ItemTouchHelper.DOWN or
									ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
					else
						0,
					(if (vh.canSwipeLeft()) ItemTouchHelper.LEFT else 0) or
							(if (vh.canSwipeRight()) ItemTouchHelper.RIGHT else 0)
			)
		}
		
		override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
			if (!(viewHolder as RecyclerViewHolder<*>).canMove()) return false
			if (!(target as RecyclerViewHolder<*>).canMove()) return false
			if (!viewHolder.canDrop(target)) return false
			val sourceIndex = viewHolder.adapterPosition
			val targetIndex = target.adapterPosition
			items.add(targetIndex, items.removeAt(sourceIndex))
			notifyItemMoved(sourceIndex, targetIndex)
			itemMoved = true
			return true
		}
		
		override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
			val vh = viewHolder as RecyclerViewHolder<*>
			when (direction) {
				ItemTouchHelper.LEFT -> vh.onSwipedLeft()
				ItemTouchHelper.RIGHT -> vh.onSwipedRight()
			}
		}
		
		override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
			super.clearView(recyclerView, viewHolder)
			if (itemMoved) {
				(viewHolder as RecyclerViewHolder<*>).onDragFinished()
				itemMoved = false
			}
		}
	}
}
