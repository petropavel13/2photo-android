package com.github.petropavel13.twophoto

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import com.facebook.drawee.backends.pipeline.Fresco
import com.github.petropavel13.twophoto.events.PostDeletedEvent
import com.github.petropavel13.twophoto.events.PostSavedEvent
import com.github.petropavel13.twophoto.fragments.PostsGridFragment
import com.github.petropavel13.twophoto.model.Post
import com.splunk.mint.Mint
import com.squareup.otto.Subscribe


public class PostsActivity : FragmentActivity(), PostsGridFragment.OnFragmentInteractionListener {
    companion object {
        val POSTS_PER_PAGE = 32
    }

    override fun onPostSelected(post: Post) {
        with(Intent(this, PostDetailActivity::class.java)) {
            putExtra(PostDetailActivity.POST_ID_KEY, post.id)
            putExtra(PostDetailActivity.FETCH_FROM_DB_KEY, fragments[viewPager?.currentItem ?: 0].useORMLiteDataSource)
            startActivity(this)
        }
    }

    override fun onError(e: Exception) {
        // TODO: handle somehow
    }

    private var viewPager: ViewPager? = null

    private val fragments = arrayOf(PostsGridFragment.newInstance(POSTS_PER_PAGE), PostsGridFragment.newInstance(POSTS_PER_PAGE, useORMLiteDataSource = true))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mint.initAndStartSession(this, "4e4a18ab")

        Fresco.initialize(this)

        setContentView(R.layout.activity_posts)

        viewPager = findViewById(R.id.posts_pager) as? ViewPager
        viewPager?.adapter = object: FragmentPagerAdapter(getSupportFragmentManager()) {
            val titles = arrayOf(getResources().getString(R.string.posts_feed), getResources().getString(R.string.posts_favorites))

            override fun getCount(): Int = fragments.count()

            override fun getItem(position: Int): Fragment? {
                with(fragments[position]) {
                    onAttach(this@PostsActivity)
                    reload()
                    return this
                }
            }

            override fun getPageTitle(position: Int): CharSequence? = titles[position]
        }

        eventsBus.register(this);
    }

    @Subscribe
    fun postAdded(event: PostSavedEvent) {
        fragments.forEach { if (it.useORMLiteDataSource) it.reload() }
    }

    @Subscribe
    fun postRemoved(event: PostDeletedEvent) {
        fragments.forEach { if (it.useORMLiteDataSource) it.reload() }
    }
}
