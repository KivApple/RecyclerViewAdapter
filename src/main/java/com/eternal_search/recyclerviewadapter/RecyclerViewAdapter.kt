package com.eternal_search.recyclerviewadapter

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.lang.IllegalArgumentException

abstract class RecyclerViewAdapter<T: RecyclerViewItem>(
		val context: Context
): RecyclerView.Adapter<RecyclerViewHolder<*>>() {
	private val viewHolderFactoriesMap = mutableMapOf<Class<out T>, MutableList<ViewHolderFactory<out T>>>()
	private val viewHolderFactories = mutableListOf<ViewHolderFactory<out T>>()
	private val viewHolders = mutableMapOf<Long, RecyclerViewHolder<*>>()
	var recyclerView: RecyclerView? = null
		private set
	val recyclerViewBackgroundColor: Int by lazy(LazyThreadSafetyMode.NONE) {
		var view = recyclerView as? View
		var color: Int = 0
		while (view != null) {
			val colorDrawable = view.background as? ColorDrawable
			if (colorDrawable != null) {
				color = colorDrawable.color
				break
			}
			view = view.parent as? View
		}
		color
	}
	
	open fun saveState(bundle: Bundle) {
	}
	
	open fun restoreState(bundle: Bundle) {
	}
	
	abstract fun getItem(index: Int): T
	
	fun registerViewType(cls: Class<out T>, viewHolderFactory: ViewHolderFactory<out T>) {
		viewHolderFactory.id = viewHolderFactories.size
		viewHolderFactories.add(viewHolderFactory)
		viewHolderFactoriesMap.getOrPut(cls, { mutableListOf() }).add(viewHolderFactory)
	}
	
	inline fun <reified T2: T>registerViewType(viewHolderFactory: ViewHolderFactory<T2>) {
		registerViewType(T2::class.java, viewHolderFactory)
	}
	
	inline fun <reified T2: T>registerViewType(
			crossinline creator: (container: ViewGroup) -> RecyclerViewHolder<T2>
	) {
		registerViewType(object : ViewHolderFactory<T2>() {
			override fun create(container: ViewGroup): RecyclerViewHolder<T2> =
					creator(container)
		})
	}
	
	inline fun <reified T2: T> registerViewType(
			crossinline creator: (container: ViewGroup) -> RecyclerViewHolder<T2>,
			crossinline filter: (item: T2) -> Boolean
	) {
		registerViewType(object : ViewHolderFactory<T2>() {
			override fun create(container: ViewGroup): RecyclerViewHolder<T2> =
					creator(container)
			
			override fun canBind(item: T2): Boolean = filter(item)
		})
	}
	
	override fun getItemId(position: Int): Long = getItem(position).id
	
	override fun getItemViewType(position: Int): Int {
		val item = getItem(position)
		val factories = viewHolderFactoriesMap[item::class.java]
		if (factories != null) {
			for (factory in factories) {
				if (factory.canBindUnsafe(item)) {
					return factory.id
				}
			}
		}
		throw IllegalArgumentException("No view holder factory for ${item::class.java.simpleName}")
	}
	
	final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder<*> =
		viewHolderFactories[viewType].create(parent)
	
	final override fun onBindViewHolder(holder: RecyclerViewHolder<*>, position: Int) {
		val item = getItem(position)
		(holder.item as? RecyclerViewItem)?.let { oldItem ->
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				viewHolders.remove(oldItem.id, holder)
			} else {
				if (viewHolders[oldItem.id] == holder) {
					viewHolders.remove(oldItem.id)
				}
			}
			null
		}
		viewHolders[item.id] = holder
		holder.bind(item)
	}
	
	override fun onViewRecycled(holder: RecyclerViewHolder<*>) {
		holder.onRecycled()
		(holder.item as? RecyclerViewItem)?.let { oldItem ->
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				viewHolders.remove(oldItem.id, holder)
			} else {
				if (viewHolders[oldItem.id] == holder) {
					viewHolders.remove(oldItem.id)
				}
			}
			null
		}
		holder.item = null
	}
	
	override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
		super.onAttachedToRecyclerView(recyclerView)
		this.recyclerView = recyclerView
	}
	
	fun getGenericViewHolder(id: Long): RecyclerViewHolder<*>? = viewHolders[id]
	
	inline fun <reified T2: RecyclerViewHolder<out T>>getViewHolder(id: Long): T2? = getGenericViewHolder(id) as? T2
	
	abstract class ViewHolderFactory<T2: RecyclerViewItem> {
		internal var id: Int = -1
		
		abstract fun create(container: ViewGroup): RecyclerViewHolder<T2>
		
		open fun canBind(item: T2): Boolean = true
		
		@Suppress("UNCHECKED_CAST")
		internal fun canBindUnsafe(item: Any) = canBind(item as T2)
	}
}
