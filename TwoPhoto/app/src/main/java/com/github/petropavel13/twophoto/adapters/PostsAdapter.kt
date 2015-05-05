package com.github.petropavel13.twophoto.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.github.petropavel13.twophoto.model.Post
import com.github.petropavel13.twophoto.views.PostItemView
import java.util.Collections
import java.util.WeakHashMap

/**
 * Created by petropavel on 25/03/15.
 */

class PostsAdapter(ctx: Context, posts: List<Post>): ArrayAdapter<Post>(ctx, PostItemView.LAYOUT_RESOURCE) {

    init {
        addAll(posts)
    }

    private val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    var _items = Collections.newSetFromMap(WeakHashMap<PostItemView, Boolean>())

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        with ((convertView ?: inflater.inflate(PostItemView.LAYOUT_RESOURCE, parent, false)) as PostItemView) {
            post = getItem(position)

            _items.add(this)

            return this
        }
    }

    fun unloadItemsImages() {
        _items.forEach { it.unloadEntryImage() }
    }

    fun loadItemsImages() {
        _items.forEach { it.loadEntryImage() }
    }
}
