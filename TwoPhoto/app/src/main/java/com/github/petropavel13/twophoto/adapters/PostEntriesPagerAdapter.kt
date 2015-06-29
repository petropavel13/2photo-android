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
import java.util.HashMap

/**
 * Created by petropavel on 31/03/15.
 */

class PostEntriesPagerAdapter(ctx: Context, var entries: List<Post.Entry>): PagerAdapter() {

    private val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var _onEntryTapListener: View.OnClickListener? = null

    var onEntryTapListener: View.OnClickListener?
        get() = _onEntryTapListener
        set(newValue) {
            _onEntryTapListener = newValue

            _positionViewMap.values().forEach { it.onTapListener = newValue }
        }

    private var _showEntriesDescription = true

    var showEntriesDescription: Boolean
        get() = _showEntriesDescription
        set(newValue) {
            _showEntriesDescription = newValue

            _positionViewMap.values().forEach { it.showDescriptionText = newValue }
        }

    private val _positionViewMap = HashMap<Int, EntryView>(3)

    fun getViewForAtPosition(position: Int): EntryView? = _positionViewMap.get(position)

    override fun getCount() = entries.count()

    override fun isViewFromObject(view: View?, obj: Any?): Boolean {
        return obj.identityEquals(view)
    }

    override fun instantiateItem(container: View?, position: Int): Any? {
        with(inflater.inflate(R.layout.entry_layout, container as? ViewGroup, false) as EntryView) {
            entry = entries[position]

            onTapListener = _onEntryTapListener
            showDescriptionText = showEntriesDescription

            (container as ViewPager).addView(this, 0)

            _positionViewMap.put(position, this)

            return this
        }
    }

    override fun destroyItem(container: View?, position: Int, item: Any?) {
        with(item as EntryView) {
            (container as ViewPager).removeView(this)

            _positionViewMap.remove(position)
        }
    }
}