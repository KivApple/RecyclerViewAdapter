package com.eternal_search.recyclerviewadapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

open class RecyclerViewHolder<T>(itemView: View): RecyclerView.ViewHolder(itemView) {
	var item: T? = null
		internal set
	
	constructor(context: Context, resId: Int, container: ViewGroup):
			this(LayoutInflater.from(context).inflate(resId, container, false))
	
	@Suppress("UNCHECKED_CAST")
	internal fun bind(item: Any) {
		val oldItem = this.item
		this.item = item as T
		onBind(item, oldItem)
	}
	
	open fun onBind(item: T, oldItem: T?) {
	}
	
	open fun onRecycled() {
	}
	
	open fun canMove(): Boolean = false
	
	open fun canDrop(target: RecyclerViewHolder<*>): Boolean = false
	
	open fun onDragFinished() {
	}
	
	open fun canSwipeLeft(): Boolean = false
	
	open fun canSwipeRight(): Boolean = false
	
	open fun onSwipedLeft() {
	}
	
	open fun onSwipedRight() {
	}
}
