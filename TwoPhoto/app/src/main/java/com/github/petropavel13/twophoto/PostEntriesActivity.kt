package com.github.petropavel13.twophoto

import android.app.Activity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.TextView
import com.github.petropavel13.twophoto.adapters.PostEntriesPagerAdapter
import com.github.petropavel13.twophoto.model.Post


public class PostEntriesActivity : Activity() {

    companion object {
        val POST_ENTRIES_KEY ="post_entries"
        val SELECTED_ENTRY_INDEX = "selected_entry_index"
    }


    var viewPager: ViewPager? = null
    var counterTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_post_entries)

        val ctx = this

        counterTextView = findViewById(R.id.post_entries_counter_text_view) as? TextView

        with(findViewById(R.id.post_entries_view_pager) as ViewPager, {
            viewPager = this

            val adapter = PostEntriesPagerAdapter(ctx, getIntent().getParcelableArrayListExtra<Post.Entry>(POST_ENTRIES_KEY))

            adapter.onEntryTapListener = object: View.OnClickListener {
                override fun onClick(view: View) {
                    if(counterTextView?.getVisibility() == View.VISIBLE) {
                        counterTextView?.setVisibility(View.GONE)

                        adapter.showEntriesDescription = false
                    } else {
                        counterTextView?.setVisibility(View.VISIBLE)

                        adapter.showEntriesDescription = true
                    }
                }
            }

            setAdapter(adapter)

            val selectedItemIndex = getIntent().getIntExtra(SELECTED_ENTRY_INDEX, 0)
            setCurrentItem(selectedItemIndex)

            counterTextView?.setText("${selectedItemIndex + 1} из ${getAdapter().getCount()}")

            setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    //
                }

                override fun onPageSelected(position: Int) {
                    counterTextView?.setText("${position + 1} из ${getAdapter().getCount()}")
                }

                override fun onPageScrollStateChanged(state: Int) {
                    //
                }
            })
        })
    }
}
