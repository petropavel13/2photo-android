package com.github.petropavel13.twophoto.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.github.petropavel13.twophoto.R
import com.github.petropavel13.twophoto.model.Post
import com.github.petropavel13.twophoto.views.PostItemView

/**
 * Created by petropavel on 25/03/15.
 */

class PostsAdapter(ctx: Context, posts: List<Post>): ArrayAdapter<Post>(ctx, R.layout.post_item_layout) {

    init {
        addAll(posts)
    }

    val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        with ((convertView ?: inflater.inflate(R.layout.post_item_layout, parent, false)) as PostItemView) {
            post = getItem(position)

            return this
        }
    }

    fun addAll(items: List<Post>?) {
        if (Build.VERSION.SDK_INT >= 11) {
            super.addAll(items)
        } else {
            items?.forEach { super.add(it) }
        }
    }
}
