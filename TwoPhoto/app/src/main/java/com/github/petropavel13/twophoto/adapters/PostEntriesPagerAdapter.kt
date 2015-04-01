package com.github.petropavel13.twophoto.adapters

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.petropavel13.twophoto.R
import com.github.petropavel13.twophoto.model.Post
import com.github.petropavel13.twophoto.views.EntryView
import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.Collections
import java.util.WeakHashMap

/**
 * Created by petropavel on 31/03/15.
 */

class PostEntriesPagerAdapter(ctx: Context, var entries: List<Post.Entry>): PagerAdapter() {

    val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    var _onEntryTapListener: View.OnClickListener? = null

    var onEntryTapListener: View.OnClickListener?
        get() = _onEntryTapListener
        set(newValue) {
            _onEntryTapListener = newValue

            _views.forEach { it?.onTapListener = newValue }
        }

    var _views = Collections.newSetFromMap(WeakHashMap<EntryView, Boolean>(getCount()))

    override fun getCount() = entries.count()

    override fun isViewFromObject(view: View?, obj: Any?): Boolean {
        return obj.identityEquals(view)
    }

    override fun instantiateItem(container: View?, position: Int): Any? {
        with(inflater.inflate(R.layout.entry_layout, null) as EntryView) {
            this.entry = entries[position]

            onTapListener = _onEntryTapListener

            (container as ViewPager).addView(this, 0)

            _views.add(this)

            return this
        }
    }

    override fun destroyItem(container: View?, position: Int, item: Any?) {
        (container as ViewPager).removeView(item as EntryView)
    }
}