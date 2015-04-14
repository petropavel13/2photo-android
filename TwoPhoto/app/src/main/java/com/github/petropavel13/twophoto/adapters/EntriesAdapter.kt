package com.github.petropavel13.twophoto.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.github.petropavel13.twophoto.R
import com.github.petropavel13.twophoto.model.Post
import com.github.petropavel13.twophoto.views.PostDetailEntryView
import java.util.WeakHashMap

/**
 * Created by petropavel on 27/03/15.
 */

class EntriesAdapter(ctx: Context, entries: List<Post.Entry>): ArrayAdapter<Post.Entry>(ctx, R.layout.post_detail_entry_item_layout) {

    init {
        addAll(entries)
    }

    val entries: Array<Post.Entry>
    get() = Array(getCount(), { getItem(it) })

    var _items = WeakHashMap<Int, PostDetailEntryView>(entries.count())

    val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        with ((convertView ?: inflater.inflate(R.layout.post_detail_entry_item_layout, parent, false)) as PostDetailEntryView) {
            entry = getItem(position)

            _items.put(position, this)

            return this
        }
    }

    fun addAll(items: List<Post.Entry>?) {
        if (Build.VERSION.SDK_INT >= 11) {
            super.addAll(items)
        } else {
            items?.forEach { super.add(it) }
        }
    }

    fun unloadItemsImages() {
        _items.values().forEach { it.unloadEntryImage() }
    }

    fun loadItemsImages() {
        _items.values().forEach { it.loadEntryImage() }
    }
}